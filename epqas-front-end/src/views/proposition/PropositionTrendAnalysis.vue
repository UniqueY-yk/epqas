<template>
  <div class="trend-analysis-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>命题质量历史趋势</span>
          <el-button type="primary" size="small" @click="loadTrendData" :loading="loading">
            刷新趋势数据
          </el-button>
        </div>
      </template>

      <div v-loading="loading">
        <template v-if="trendData.length > 0">
          <div class="chart-wrapper">
            <v-chart class="trend-chart" :option="chartOption" autoresize />
          </div>
          <div class="analysis-summary">
            <el-alert
              title="趋势分析说明"
              type="info"
              show-icon
              :closable="false"
            >
              <template #default>
                <ul>
                  <li><strong>整体难度 (Difficulty):</strong> 理想值通常在 0.5 - 0.7 之间。持续走低（难）或走高（易）说明命题标尺可能发生了偏移。</li>
                  <li><strong>区分度 (Discrimination):</strong> 越高越好，推荐保持在 0.3 以上。若持续降低，请重视 AI 的优化建议。</li>
                  <li><strong>信度系数 (Reliability - Cronbach's Alpha):</strong> 反映试卷稳定性，推荐 > 0.7。</li>
                  <li><strong>效度 (Validity):</strong> 反映考试是否真正测量了预定要测考的知识和能力，推荐 > 0.4。</li>
                </ul>
              </template>
            </el-alert>
          </div>
        </template>
        <el-empty v-else-if="!loading" description="暂无历史命题分析数据，请先前往 [试卷质量诊断] 完成考试结算计算。" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPropositionTrend, type PaperAnalysisVO } from '../../api/analysis'

// Register ECharts core components manually if not globally registered
import { use } from 'echarts/core'
import { TitleComponent, TooltipComponent, GridComponent, LegendComponent, DataZoomComponent, MarkLineComponent } from 'echarts/components'
import { LineChart } from 'echarts/charts'
import { CanvasRenderer } from 'echarts/renderers'
import VChart from 'vue-echarts'
import dayjs from 'dayjs'

use([
    TitleComponent,
    TooltipComponent,
    GridComponent,
    LegendComponent,
    DataZoomComponent,
    MarkLineComponent,
    LineChart,
    CanvasRenderer
])

const loading = ref(false)
const trendData = ref<PaperAnalysisVO[]>([])

const loadTrendData = async () => {
    loading.value = true
    try {
        // Fetch setterId from local storage
        const userIdStr = localStorage.getItem('userId')
        const setterId = userIdStr ? Number(userIdStr) : 2 // Fallback to setter1 ID

        if (!setterId) {
            ElMessage.error('无法获取当前命题教师身份信息')
            return
        }

        const res = await getPropositionTrend(setterId)
        trendData.value = res.data || []
    } catch (error: any) {
        ElMessage.error(error.message || '获取命题趋势数据失败')
    } finally {
        loading.value = false
    }
}

onMounted(() => {
    loadTrendData()
})

const chartOption = computed(() => {
    if (trendData.value.length === 0) return {}

    const xAxisLabels = trendData.value.map(item => {
        const dateStr = item.examDate ? dayjs(item.examDate).format('YYYY-MM-DD') : '未定日期'
        let title = item.paperTitle || '未知试卷';
        if (title.length > 15) {
            title = title.substring(0, 15) + '...'
        }
        return `${title}\n(${dateStr})`
    })

    const difficultySeries = trendData.value.map(item => item.overallDifficulty)
    const discriminationSeries = trendData.value.map(item => item.overallDiscrimination)
    const reliabilitySeries = trendData.value.map(item => item.reliabilityCoefficient)
    const validitySeries = trendData.value.map(item => item.validityCoefficient)

    return {
        color: ['#E6A23C', '#67C23A', '#409EFF', '#9C27B0'],
        tooltip: {
            trigger: 'axis',
            axisPointer: { type: 'cross', label: { backgroundColor: '#6a7985' } },
            backgroundColor: 'rgba(255, 255, 255, 0.95)',
            borderColor: '#e2e8f0',
            borderWidth: 1,
            padding: 12,
            textStyle: { color: '#333' }
        },
        legend: {
            data: ['整体难度 (Difficulty)', '区分度 (Discrimination)', '信度 (Reliability)', '效度 (Validity)'],
            bottom: '0%',
            icon: 'roundRect',
            itemGap: 20
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '22%',
            containLabel: true
        },
        xAxis: {
            type: 'category',
            boundaryGap: false,
            data: xAxisLabels,
            axisLabel: {
                interval: 0,
                rotate: 20, // Rotate to fit long paper titles
                fontSize: 12,
                color: '#666',
                margin: 15
            },
            axisLine: { lineStyle: { color: '#dcdfe6' } }
        },
        yAxis: {
            type: 'value',
            min: -0.2, // Allow discrimination to show negatives
            max: 1.0,
            interval: 0.2,
            axisLabel: {
                formatter: '{value}',
                color: '#666'
            },
            splitLine: {
                lineStyle: { type: 'dashed', color: '#ebeef5' }
            }
        },
        dataZoom: [
            {
                type: 'inside',
                start: 0,
                end: 100
            },
            {
                start: 0,
                end: 100,
                bottom: 35,
                height: 20,
                borderColor: 'transparent',
                backgroundColor: '#f5f7fa',
                fillerColor: 'rgba(64, 158, 255, 0.2)'
            }
        ],
        series: [
            {
                name: '整体难度 (Difficulty)',
                type: 'line',
                data: difficultySeries,
                smooth: true,
                symbol: 'circle',
                symbolSize: 8,
                itemStyle: { color: '#E6A23C' }, // Warning color
                lineStyle: { width: 3, shadowColor: 'rgba(230,162,60,0.3)', shadowBlur: 10, shadowOffsetY: 5 },
                areaStyle: {
                    color: {
                        type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
                        colorStops: [{ offset: 0, color: 'rgba(230,162,60,0.2)' }, { offset: 1, color: 'rgba(230,162,60,0)' }]
                    }
                },
                markLine: {
                    data: [{ type: 'average', name: 'Avg' }],
                    lineStyle: { type: 'dashed', color: '#E6A23C' },
                    label: { position: 'end', formatter: '平均难度: {c}' }
                }
            },
            {
                name: '区分度 (Discrimination)',
                type: 'line',
                data: discriminationSeries,
                smooth: true,
                symbol: 'circle',
                symbolSize: 8,
                itemStyle: { color: '#67C23A' }, // Success color
                lineStyle: { width: 3, shadowColor: 'rgba(103,194,58,0.3)', shadowBlur: 10, shadowOffsetY: 5 },
                areaStyle: {
                    color: {
                        type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
                        colorStops: [{ offset: 0, color: 'rgba(103,194,58,0.2)' }, { offset: 1, color: 'rgba(103,194,58,0)' }]
                    }
                }
            },
            {
                name: '信度 (Reliability)',
                type: 'line',
                data: reliabilitySeries,
                smooth: true,
                symbol: 'circle',
                symbolSize: 8,
                itemStyle: { color: '#409EFF' }, // Primary color
                lineStyle: { width: 3, shadowColor: 'rgba(64,158,255,0.3)', shadowBlur: 10, shadowOffsetY: 5 },
                areaStyle: {
                    color: {
                        type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
                        colorStops: [{ offset: 0, color: 'rgba(64,158,255,0.2)' }, { offset: 1, color: 'rgba(64,158,255,0)' }]
                    }
                }
            },
            {
                name: '效度 (Validity)',
                type: 'line',
                data: validitySeries,
                smooth: true,
                symbol: 'circle',
                symbolSize: 8,
                itemStyle: { color: '#9C27B0' }, // Purple color
                lineStyle: { width: 3, shadowColor: 'rgba(156,39,176,0.3)', shadowBlur: 10, shadowOffsetY: 5 },
                areaStyle: {
                    color: {
                        type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
                        colorStops: [{ offset: 0, color: 'rgba(156,39,176,0.2)' }, { offset: 1, color: 'rgba(156,39,176,0)' }]
                    }
                }
            }
        ]
    }
})
</script>

<style scoped>
.trend-analysis-container {
    padding: 20px;
}
.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: bold;
}
.chart-wrapper {
    height: 500px;
    width: 100%;
}
.trend-chart {
    height: 100%;
    width: 100%;
}
.analysis-summary {
    margin-top: 20px;
}
.analysis-summary ul {
    margin: 5px 0 0 0;
    padding-left: 20px;
}
.analysis-summary li {
    margin-bottom: 5px;
    line-height: 1.5;
}
</style>
