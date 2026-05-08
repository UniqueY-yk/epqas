<template>
  <el-dialog
    v-model="dialogVisible"
    :title="`题目质量异动监控 - ${examTitle}`"
    width="1050px"
    destroy-on-close
    @opened="handleOpened"
  >
    <div v-loading="loading" class="chart-container">
      <div v-if="!chartData.length && !loading" class="empty-state">
        <el-empty description="暂无题目分析数据，请确保试卷已重新计算指标。" />
      </div>
      
      <v-chart
        v-if="chartData.length"
        class="scatter-chart"
        :option="chartOption"
        autoresize
        @click="handleChartClick"
      />
    </div>

    <!-- Detailed Data Table for Accurate Reading -->
    <div v-if="chartData.length" class="detail-table-section">
      <p class="table-title">📋 全部题目指标明细 <span class="badge">{{ chartData.length }}题</span></p>
      <el-table :data="chartData" size="small" stripe border style="width: 100%" max-height="260">
        <el-table-column label="题号" width="70" align="center">
          <template #default="scope">
            <span class="q-badge">Q{{ scope.row.questionId }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="questionType" label="类型" width="80" align="center" />
        <el-table-column label="难度P" width="130" align="center">
          <template #default="scope">
            <span :style="{ color: getDifficultyColor(scope.row.difficultyIndex) }">
              {{ (scope.row.difficultyIndex ?? scope.row.correctResponseRate)?.toFixed(3) }}
            </span>
            <el-tag v-if="scope.row.difficultyEvaluation" size="small" :type="getDifficultyTagType(scope.row.difficultyIndex)" style="margin-left: 4px;">
              {{ scope.row.difficultyEvaluation }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="区分度D" width="140" align="center">
          <template #default="scope">
            <span :style="{ color: getDiscColor(scope.row.discriminationIndex) }">
              {{ scope.row.discriminationIndex?.toFixed(3) }}
            </span>
            <el-tag v-if="scope.row.discriminationEvaluation" size="small" :type="getDiscTagType(scope.row.discriminationIndex)" style="margin-left: 4px;">
              {{ scope.row.discriminationEvaluation }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="效度r" width="80" align="center">
          <template #default="scope">
            {{ scope.row.validityIndex?.toFixed(3) ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.isAbnormal ? 'danger' : 'success'" size="small">
              {{ scope.row.isAbnormal ? '异动' : '达标' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="诊断标签" min-width="160" show-overflow-tooltip>
          <template #default="scope">
            <span style="color: #E6A23C;">{{ scope.row.diagnosisTag || '-' }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <div class="legend-info">
      <p>💡 <b>分析说明:</b></p>
      <ul>
        <li><span class="dot normal"></span> <b>绿色点:</b> 质量达标的正常题目。</li>
        <li><span class="dot abnormal"></span> <b>红色点:</b> 质量异常题目（如区分度过低或难度极其失衡），建议命题人重点关注并修正。</li>
        <li><b>横坐标(X):</b> 难度系数P (极端组法) - 值越高代表题目越简单。</li>
        <li><b>纵坐标(Y):</b> 区分度D - 值越高代表题目越能区分高低分考生。</li>
        <li>⚠️ <b>重叠处理:</b> 当多个题目的P/D值相同或极其接近时，散点图会自动加入微小偏移以分开显示。实际精确值请参照下方表格。</li>
      </ul>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">关闭窗口</el-button>
      </span>
    </template>
  </el-dialog>
  
  <PaperSuggestionsDrawer ref="suggestionsDrawerRef" />
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { use } from 'echarts/core'
import { ScatterChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, GridComponent, DatasetComponent, GraphicComponent, MarkAreaComponent, DataZoomComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import VChart from 'vue-echarts'
import { getQuestionAnalysisByPaperId, type QuestionAnalysisVO } from '../../api/analysis'
import PaperSuggestionsDrawer from './PaperSuggestionsDrawer.vue'

// Register ECharts core components
use([
  TitleComponent,
  TooltipComponent,
  GridComponent,
  DatasetComponent,
  ScatterChart,
  CanvasRenderer,
  GraphicComponent,
  MarkAreaComponent,
  DataZoomComponent
])

const dialogVisible = ref(false)
const loading = ref(false)
const paperId = ref<number>(0)
const examTitle = ref<string>('')
const chartData = ref<QuestionAnalysisVO[]>([])
const suggestionsDrawerRef = ref<InstanceType<typeof PaperSuggestionsDrawer> | null>(null)

// Expose open method to parent component
const openDialog = (id: number, title: string) => {
  paperId.value = id
  examTitle.value = title
  dialogVisible.value = true
}

defineExpose({ openDialog })

const handleOpened = async () => {
    loading.value = true
    try {
        const res = await getQuestionAnalysisByPaperId(paperId.value)
        chartData.value = res.data || []
    } catch (e: any) {
        ElMessage.error(e.message || '获取分析数据失败')
    } finally {
        loading.value = false
    }
}

/**
 * Jitter algorithm: detect overlapping points and apply sunflower-pattern offsets.
 * Points with the same (P, D) rounded to 2 decimals are considered overlapping.
 * Returns display coordinates [jitteredP, jitteredD] while preserving raw data for tooltips.
 */
const applyJitter = (items: QuestionAnalysisVO[]) => {
    const JITTER_RADIUS = 0.025 // base offset radius in chart units
    const buckets = new Map<string, { items: QuestionAnalysisVO[], indices: number[] }>()
    
    // Group items by rounded coordinates
    items.forEach((item, idx) => {
        const px = (item.difficultyIndex ?? item.correctResponseRate ?? 0)
        const py = item.discriminationIndex ?? 0
        const key = `${px.toFixed(2)}_${py.toFixed(2)}`
        if (!buckets.has(key)) {
            buckets.set(key, { items: [], indices: [] })
        }
        buckets.get(key)!.items.push(item)
        buckets.get(key)!.indices.push(idx)
    })
    
    const result: { value: [number, number], rawData: QuestionAnalysisVO }[] = []
    
    for (const [, bucket] of buckets) {
        const count = bucket.items.length
        bucket.items.forEach((item, i) => {
            const baseX = item.difficultyIndex ?? item.correctResponseRate ?? 0
            const baseY = item.discriminationIndex ?? 0
            
            let displayX = baseX
            let displayY = baseY
            
            if (count > 1) {
                // Sunflower pattern: distribute points in a circle around the center
                const angle = (2 * Math.PI * i) / count
                const radius = JITTER_RADIUS * (1 + Math.floor(i / 6) * 0.5) // expand radius for many points
                displayX = baseX + radius * Math.cos(angle)
                displayY = baseY + radius * Math.sin(angle)
            }
            
            // Clamp to chart bounds
            displayX = Math.max(0, Math.min(1.0, displayX))
            displayY = Math.max(-1.0, Math.min(1.0, displayY))
            
            result.push({
                value: [displayX, displayY],
                rawData: item
            })
        })
    }
    
    return result
}

// Compute ECharts Option
const chartOption = computed(() => {
    const jitteredData = applyJitter(chartData.value)
    
    return {
        title: {
            text: '试卷试题分布散点图',
            subtext: `共 ${chartData.value.length} 道题目`,
            left: 'center',
            textStyle: { fontSize: 16 },
            subtextStyle: { fontSize: 12, color: '#909399' }
        },
        tooltip: {
            enterable: true,
            extraCssText: 'box-shadow: 0 4px 16px rgba(0,0,0,0.1); border-radius: 8px; padding: 12px;',
            formatter: function (params: any) {
                const data: QuestionAnalysisVO = params.data.rawData;
                // Show ACTUAL values, not jittered display values
                const actualP = data.difficultyIndex ?? data.correctResponseRate ?? 0
                const actualD = data.discriminationIndex ?? 0
                
                let suggestionHtml = '';
                if (data.suggestions && data.suggestions.length > 0) {
                    suggestionHtml = `
                        <hr style="margin: 8px 0; border: 0; border-top: 1px dashed #e4e7ed;" />
                        <div style="color: #E6A23C; font-weight: bold; margin-bottom: 4px;">💡 教学优化建议:</div>
                        <ul style="margin: 0; padding-left: 18px; font-size: 12px; color: #E6A23C; line-height: 1.5;">
                            ${data.suggestions.map((s: string) => `<li style="margin-bottom: 2px;">${s}</li>`).join('')}
                        </ul>
                    `;
                }

                let distributionHtml = '';
                if (data.selectionDistributionJson) {
                    try {
                        const dist = JSON.parse(data.selectionDistributionJson);
                        const keys = Object.keys(dist).sort();
                        if (keys.length > 0) {
                            distributionHtml = `
                                <hr style="margin: 8px 0; border: 0; border-top: 1px solid #e4e7ed;" />
                                <div style="margin-bottom: 6px;"><b>📊 选项分布:</b></div>
                                <div style="font-size: 12px; display: flex; flex-wrap: wrap; gap: 6px;">
                                    ${keys.map(k => `<span style="background: #f0f2f5; padding: 3px 8px; border-radius: 4px; color: #606266; font-weight: 500;">${k}: <b style="color: #409EFF;">${dist[k]}</b></span>`).join('')}
                                </div>
                            `;
                        }
                    } catch(e) {}
                }

                return `
                    <div style="max-width:360px; white-space:normal;">
                        <b style="font-size: 15px; color: #303133;">📖 [题目${data.questionId}]</b> <br/>
                        <div style="color: #606266; margin-top: 8px; margin-bottom: 8px; line-height: 1.4;">${data.stem || '暂无题目内容'}</div>
                        <hr style="margin: 8px 0; border: 0; border-top: 1px solid #e4e7ed;" />
                        <div style="display: flex; justify-content: space-between; margin-top: 6px; margin-bottom: 6px; font-size: 13px;">
                            <span style="color: #606266;">难度P: <b style="color: #303133;">${actualP.toFixed(3)}</b> ${data.difficultyEvaluation ? '<span style="background: #fdf6ec; color: #e6a23c; padding: 1px 4px; border-radius: 3px; font-size: 11px;">' + data.difficultyEvaluation + '</span>' : ''}</span>
                            <span style="color: #606266;">区分度D: <b style="color: #303133;">${actualD.toFixed(3)}</b> ${data.discriminationEvaluation ? '<span style="background: #fdf6ec; color: #e6a23c; padding: 1px 4px; border-radius: 3px; font-size: 11px;">' + data.discriminationEvaluation + '</span>' : ''}</span>
                        </div>
                        <div style="font-size: 13px; margin-bottom: 4px;">
                            状态标签: <strong style="background: ${data.isAbnormal ? '#fef0f0' : '#f0f9eb'}; color: ${data.isAbnormal ? '#F56C6C' : '#67C23A'}; padding: 2px 6px; border-radius: 4px; font-size: 12px; border: 1px solid ${data.isAbnormal ? '#fde2e2' : '#e1f3d8'};">
                                    ${data.isAbnormal ? '🔴 异动警告' : '🟢 运行达标'}
                                  </strong>
                        </div>
                        ${distributionHtml}
                        ${suggestionHtml}
                    </div>
                `
            }
        },
        grid: {
            left: 60,
            right: 80,
            bottom: 95,
            top: 70
        },
        xAxis: {
            type: 'value',
            name: '难度P (极端组法)',
            nameLocation: 'middle',
            nameGap: 35,
            min: 0,
            max: 1.0,
            interval: 0.1,
            splitLine: { lineStyle: { type: 'dashed', color: '#e8e8e8' } }
        },
        yAxis: {
            type: 'value',
            name: '区分度D',
            nameLocation: 'middle',
            nameGap: 35,
            min: -1.0,
            max: 1.0,
            interval: 0.2,
            splitLine: { lineStyle: { type: 'dashed', color: '#e8e8e8' } }
        },
        series: [
            {
                type: 'scatter',
                symbolSize: 18,
                itemStyle: {
                    color: function(params: any) {
                        return params.data.rawData.isAbnormal ? '#F56C6C' : '#67C23A';
                    },
                    opacity: 0.9,
                    borderColor: '#fff',
                    borderWidth: 1.5
                },
                label: {
                    show: true,
                    formatter: (params: any) => 'Q' + params.data.rawData.questionId,
                    position: 'right',
                    fontSize: 11,
                    color: '#303133',
                    fontWeight: 'bold',
                    distance: 8
                },
                emphasis: {
                    itemStyle: {
                        shadowBlur: 12,
                        shadowColor: 'rgba(0, 0, 0, 0.3)',
                        opacity: 1,
                        borderWidth: 2
                    },
                    label: {
                        show: true,
                        fontSize: 13,
                        fontWeight: 'bold',
                        color: '#409EFF'
                    }
                },
                // Mark quality zone areas
                markArea: {
                    silent: true,
                    data: [
                        // Green zone: moderate difficulty + good discrimination
                        [
                            { xAxis: 0.4, yAxis: 0.3, itemStyle: { color: 'rgba(103, 194, 58, 0.06)' } },
                            { xAxis: 0.7, yAxis: 1.0 }
                        ],
                        // Red zone: low discrimination
                        [
                            { xAxis: 0, yAxis: -1.0, itemStyle: { color: 'rgba(245, 108, 108, 0.06)' } },
                            { xAxis: 1.0, yAxis: 0.2 }
                        ]
                    ]
                },
                data: jitteredData
            }
        ],
        dataZoom: [
            {
                type: 'inside',
                xAxisIndex: 0,
                filterMode: 'none'
            },
            {
                type: 'inside',
                yAxisIndex: 0,
                filterMode: 'none'
            },
            {
                type: 'slider',
                xAxisIndex: 0,
                filterMode: 'none',
                bottom: 5,
                height: 24
            },
            {
                type: 'slider',
                yAxisIndex: 0,
                filterMode: 'none',
                right: 10
            }
        ]
    }
})

const handleChartClick = (params: any) => {
    if (params.componentType === 'series' && params.seriesType === 'scatter') {
        const questionId = params.data?.rawData?.questionId
        if (questionId && paperId.value) {
            suggestionsDrawerRef.value?.openDrawer(paperId.value, questionId)
        }
    }
}

// Table helpers
const getDifficultyColor = (val: number) => {
    if (val == null) return '#606266'
    if (val < 0.4) return '#67C23A'
    if (val > 0.7) return '#F56C6C'
    return '#606266'
}
const getDifficultyTagType = (val: number) => {
    if (val == null) return 'info'
    if (val >= 0.4 && val <= 0.7) return 'success'
    return 'warning'
}
const getDiscColor = (val: number) => {
    if (val == null) return '#606266'
    if (val < 0.2) return '#F56C6C'
    if (val >= 0.4) return '#67C23A'
    return '#606266'
}
const getDiscTagType = (val: number) => {
    if (val == null) return 'info'
    if (val >= 0.4) return 'success'
    if (val >= 0.3) return ''
    if (val >= 0.2) return 'warning'
    return 'danger'
}
</script>

<style scoped>
.chart-container {
    height: 520px;
    width: 100%;
}
.scatter-chart {
    height: 100%;
    width: 100%;
}
.empty-state {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100%;
}

.detail-table-section {
    margin-top: 16px;
    background: #fafbfc;
    border-radius: 8px;
    padding: 14px 16px;
    border: 1px solid #ebeef5;
}

.table-title {
    margin: 0 0 12px 0;
    font-size: 14px;
    font-weight: 600;
    color: #303133;
}

.badge {
    display: inline-block;
    background: #409EFF;
    color: white;
    font-size: 11px;
    padding: 1px 8px;
    border-radius: 10px;
    margin-left: 6px;
    font-weight: 500;
}

.q-badge {
    display: inline-block;
    background: #f0f2f5;
    color: #303133;
    font-weight: 700;
    font-size: 12px;
    padding: 2px 8px;
    border-radius: 4px;
}

.legend-info {
    margin-top: 16px;
    padding: 15px;
    background-color: #f8f9fc;
    border-radius: 6px;
    font-size: 13px;
    color: #555;
    line-height: 1.6;
}

.legend-info p {
    margin-top: 0;
    margin-bottom: 8px;
}

.legend-info ul {
    margin: 0;
    padding-left: 20px;
}

.dot {
    display: inline-block;
    width: 10px;
    height: 10px;
    border-radius: 50%;
    margin-right: 4px;
}
.dot.normal { background-color: #67C23A; }
.dot.abnormal { background-color: #F56C6C; }
</style>
