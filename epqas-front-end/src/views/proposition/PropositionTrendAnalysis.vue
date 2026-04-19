zho<template>
  <div class="trend-analysis-container examination-paper-management">
    <div class="page-header">
      <h2>历史命题趋势</h2>
      <p class="subtitle">追踪和分析历次考试命题各项质量指标的变化曲线</p>
    </div>

    <!-- Toolbar -->
    <el-card class="toolbar-card" shadow="hover">
      <div class="toolbar" style="justify-content: flex-start;">
        <div class="search-area">
          <el-select 
            v-if="isAdmin" 
            v-model="selectedSetterId" 
            placeholder="请选择命题教师" 
            clearable
            class="search-input"
            @change="handleFilterChange"
          >
            <el-option
              v-for="setter in setterOptions"
              :key="setter.userId"
              :label="setter.username + (setter.realName ? ' (' + setter.realName + ')' : '')"
              :value="setter.userId"
            />
          </el-select>

          <el-select 
            v-model="selectedCourseId" 
            placeholder="请选择课程" 
            clearable
            class="search-input"
            @change="handleFilterChange"
          >
            <el-option
              v-for="course in courseOptions"
              :key="course.courseId"
              :label="course.courseName"
              :value="course.courseId"
            />
          </el-select>

          <el-button type="primary" @click="loadTrendData" :loading="loading" :disabled="!canLoadTrend">
            <el-icon><RefreshRight /></el-icon> 刷新趋势数据
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- Main Content -->
    <el-card shadow="hover" class="table-card">
      <div v-loading="loading">
        <template v-if="trendData.length > 0">
          <div class="chart-wrapper">
            <v-chart class="trend-chart" :option="chartOption" autoresize />
          </div>

          <!-- Prediction Card -->
          <div class="prediction-section" v-if="prediction">
            <div class="prediction-header">
              <div class="prediction-title">
                <span class="prediction-icon">📊</span>
                <span>下一套试卷指标预测</span>
              </div>
              <div class="prediction-meta">
                <el-tag size="small" type="info" effect="plain">
                  算法: {{ prediction.method === 'OLS' ? '线性回归 (OLS)' : '加权移动平均 (WMA)' }}
                </el-tag>
                <el-tag size="small" type="info" effect="plain" style="margin-left: 8px;">
                  基于 {{ prediction.dataPointsUsed }} 次历史数据
                </el-tag>
              </div>
            </div>

            <div class="prediction-cards">
              <div class="prediction-card difficulty-card">
                <div class="indicator-label">整体难度</div>
                <div class="indicator-value">{{ formatValue(prediction.predictedDifficulty) }}</div>
                <div class="indicator-trend" :class="trendClass(prediction.difficultyTrend)">
                  {{ trendArrow(prediction.difficultyTrend) }} {{ prediction.difficultyTrend }}
                </div>
                <div class="indicator-range">
                  [{{ formatValue(prediction.difficultyLower) }} ~ {{ formatValue(prediction.difficultyUpper) }}]
                </div>
              </div>

              <div class="prediction-card discrimination-card">
                <div class="indicator-label">区分度</div>
                <div class="indicator-value">{{ formatValue(prediction.predictedDiscrimination) }}</div>
                <div class="indicator-trend" :class="trendClass(prediction.discriminationTrend)">
                  {{ trendArrow(prediction.discriminationTrend) }} {{ prediction.discriminationTrend }}
                </div>
                <div class="indicator-range">
                  [{{ formatValue(prediction.discriminationLower) }} ~ {{ formatValue(prediction.discriminationUpper) }}]
                </div>
              </div>

              <div class="prediction-card reliability-card">
                <div class="indicator-label">信度系数</div>
                <div class="indicator-value">{{ formatValue(prediction.predictedReliability) }}</div>
                <div class="indicator-trend" :class="trendClass(prediction.reliabilityTrend)">
                  {{ trendArrow(prediction.reliabilityTrend) }} {{ prediction.reliabilityTrend }}
                </div>
                <div class="indicator-range">
                  [{{ formatValue(prediction.reliabilityLower) }} ~ {{ formatValue(prediction.reliabilityUpper) }}]
                </div>
              </div>

              <div class="prediction-card validity-card">
                <div class="indicator-label">效度</div>
                <div class="indicator-value">{{ formatValue(prediction.predictedValidity) }}</div>
                <div class="indicator-trend" :class="trendClass(prediction.validityTrend)">
                  {{ trendArrow(prediction.validityTrend) }} {{ prediction.validityTrend }}
                </div>
                <div class="indicator-range">
                  [{{ formatValue(prediction.validityLower) }} ~ {{ formatValue(prediction.validityUpper) }}]
                </div>
              </div>
            </div>

            <!-- Smart Advice -->
            <div class="prediction-advice" v-if="adviceList.length > 0">
              <el-alert title="💡 命题建议" type="warning" show-icon :closable="false">
                <template #default>
                  <ul>
                    <li v-for="(advice, idx) in adviceList" :key="idx">{{ advice }}</li>
                  </ul>
                </template>
              </el-alert>
            </div>
          </div>

          <!-- Insufficient data for prediction -->
          <div class="prediction-section" v-else-if="!predictionLoading && trendData.length < 2">
            <el-empty description="至少需要 2 次历史考试数据才能进行趋势预测" :image-size="80" />
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
        <el-empty v-else-if="!loading" :description="emptyDescription" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { RefreshRight } from '@element-plus/icons-vue'
import { getPropositionTrend, getTrendPrediction, type PaperAnalysisVO, type TrendPredictionVO } from '../../api/analysis'
import { getSetters, type SetterInfo } from '../../api/exam'
import { getCourses } from '../../api/academic'

// Register ECharts core components manually if not globally registered
import { use } from 'echarts/core'
import { TitleComponent, TooltipComponent, GridComponent, LegendComponent, DataZoomComponent, MarkLineComponent, MarkAreaComponent } from 'echarts/components'
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
    MarkAreaComponent,
    LineChart,
    CanvasRenderer
])

const loading = ref(false)
const predictionLoading = ref(false)
const trendData = ref<PaperAnalysisVO[]>([])
const prediction = ref<TrendPredictionVO | null>(null)
const currentRoleId = Number(localStorage.getItem('roleId') || '1')
const isAdmin = currentRoleId === 1

const selectedSetterId = ref<number | undefined>(undefined)
const setterOptions = ref<SetterInfo[]>([])
const selectedCourseId = ref<number | undefined>(undefined)
const courseOptions = ref<any[]>([])

// 是否可以加载趋势数据：必须同时选择命题教师和课程
const canLoadTrend = computed(() => {
    if (isAdmin) {
        return !!selectedSetterId.value && !!selectedCourseId.value
    }
    return !!selectedCourseId.value
})

// 空状态提示语
const emptyDescription = computed(() => {
    if (!canLoadTrend.value) {
        if (isAdmin) {
            return '请先选择一位命题教师和课程，再查看历史命题趋势'
        }
        return '请先选择一门课程，再查看历史命题趋势'
    }
    return '暂无历史命题分析数据，请先前往 [试卷质量诊断] 完成考试结算计算。'
})

// Format number to 2 decimal places
const formatValue = (val: number | null | undefined) => {
    if (val === null || val === undefined) return '-'
    return val.toFixed(2)
}

// Trend direction arrow
const trendArrow = (trend: string) => {
    if (!trend) return '→'
    if (trend.includes('上升')) return '↗'
    if (trend.includes('下降')) return '↘'
    return '→'
}

// Trend CSS class
const trendClass = (trend: string) => {
    if (!trend) return 'trend-stable'
    if (trend.includes('上升') && trend.includes('提升')) return 'trend-up-good'
    if (trend.includes('上升') && trend.includes('趋易')) return 'trend-up-warn'
    if (trend.includes('下降') && trend.includes('下降')) return 'trend-down-warn'
    if (trend.includes('下降') && trend.includes('趋难')) return 'trend-down-warn'
    return 'trend-stable'
}

// Smart advice generation
const adviceList = computed(() => {
    if (!prediction.value) return []
    const p = prediction.value
    const list: string[] = []

    // Difficulty advice
    if (p.predictedDifficulty < 0.4) {
        list.push('预测难度偏高（试卷偏难），建议下次命题适当增加基础题比例，降低综合分析类题目难度。')
    } else if (p.predictedDifficulty > 0.7) {
        list.push('预测难度偏低（试卷偏易），建议下次命题增加有一定挑战性的题目，避免区分度不足。')
    } else {
        list.push('预测难度在合理范围内（0.4-0.7），建议维持当前命题难度风格。')
    }

    // Discrimination advice
    if (p.predictedDiscrimination < 0.2) {
        list.push('预测区分度较低，建议优化选项设置，增加干扰项的合理性，减少"送分题"。')
    } else if (p.predictedDiscrimination < 0.3) {
        list.push('预测区分度一般，可适当调整部分题目的考核方式以提升区分效果。')
    }

    // Reliability advice
    if (p.predictedReliability < 0.6) {
        list.push('预测信度系数偏低，建议增加题目数量或提高题目间的内部一致性。')
    } else if (p.predictedReliability < 0.7) {
        list.push('预测信度系数处于中等水平，建议关注题目之间的一致性。')
    }

    // Validity advice
    if (p.predictedValidity < 0.4) {
        list.push('预测效度偏低，建议加强试题与教学大纲知识点的对应关系，确保考核内容的针对性。')
    }

    return list
})

const loadTrendData = async () => {
    // 必须同时选择命题教师和课程
    if (!canLoadTrend.value) {
        trendData.value = []
        prediction.value = null
        return
    }

    loading.value = true
    prediction.value = null
    try {
        let setterId: number | undefined
        if (!isAdmin) {
            const userIdStr = localStorage.getItem('userId')
            setterId = userIdStr ? Number(userIdStr) : 0

            if (!setterId) {
                ElMessage.error('无法获取当前命题教师身份信息')
                return
            }
        } else {
            setterId = selectedSetterId.value
        }

        const res = await getPropositionTrend(setterId, selectedCourseId.value)
        trendData.value = res.data || []

        // Load prediction data if we have trend data
        if (trendData.value.length >= 2) {
            predictionLoading.value = true
            try {
                const predRes = await getTrendPrediction(setterId, selectedCourseId.value)
                prediction.value = predRes.data || null
            } catch (predError: any) {
                console.warn('Prediction failed:', predError.message)
            } finally {
                predictionLoading.value = false
            }
        }
    } catch (error: any) {
        ElMessage.error(error.message || '获取命题趋势数据失败')
    } finally {
        loading.value = false
    }
}

const handleFilterChange = () => {
    loadTrendData()
}

onMounted(async () => {
    if (isAdmin) {
        try {
            const [setterRes, courseRes] = await Promise.all([
                getSetters(),
                getCourses({ current: 1, size: 100 })
            ])
            setterOptions.value = setterRes.data || []
            courseOptions.value = courseRes.data.records || []
        } catch (error: any) {
            ElMessage.error('获取初始筛选列表失败')
        }
    } else {
        // 非管理员：加载课程列表，但不自动加载趋势数据（需要先选择课程）
        try {
            const courseRes = await getCourses({ current: 1, size: 100 })
            courseOptions.value = courseRes.data.records || []
        } catch (e) {}
    }
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

    // Append prediction point if available
    if (prediction.value) {
        xAxisLabels.push('下一次 (预测)')
        difficultySeries.push(prediction.value.predictedDifficulty)
        discriminationSeries.push(prediction.value.predictedDiscrimination)
        reliabilitySeries.push(prediction.value.predictedReliability)
        validitySeries.push(prediction.value.predictedValidity)
    }

    const totalPoints = xAxisLabels.length
    const lastRealIndex = prediction.value ? totalPoints - 2 : totalPoints - 1

    // Build series with prediction styling
    const buildSeries = (name: string, data: any[], color: string, shadowColor: string, areaColorStart: string): any => {
        const seriesConfig: any = {
            name,
            type: 'line',
            data: data.map((val, idx) => {
                if (prediction.value && idx === totalPoints - 1) {
                    // Prediction point: special styling
                    return {
                        value: val,
                        symbol: 'diamond',
                        symbolSize: 12,
                        itemStyle: {
                            color: color,
                            borderColor: '#fff',
                            borderWidth: 2,
                            shadowColor: shadowColor,
                            shadowBlur: 10
                        }
                    }
                }
                return val
            }),
            smooth: true,
            symbol: 'circle',
            symbolSize: 8,
            itemStyle: { color },
            lineStyle: { width: 3, shadowColor, shadowBlur: 10, shadowOffsetY: 5 },
            areaStyle: {
                color: {
                    type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
                    colorStops: [{ offset: 0, color: areaColorStart }, { offset: 1, color: areaColorStart.replace(/[\d.]+\)$/, '0)') }]
                }
            }
        }

        // Add markLine only for difficulty series
        if (name.includes('难度')) {
            seriesConfig.markLine = {
                data: [{ type: 'average', name: 'Avg' }],
                lineStyle: { type: 'dashed', color },
                label: { position: 'end', formatter: '平均难度: {c}' }
            }
        }

        return seriesConfig
    }

    // If there's a prediction point, we create visual split with dashed line
    const visualMapPieces = prediction.value ? [
        { gt: -1, lte: lastRealIndex, color: undefined }, // keep default
        { gt: lastRealIndex, lte: totalPoints - 1, color: undefined, lineStyle: { type: 'dashed', width: 2 } }
    ] : undefined

    const option: any = {
        color: ['#E6A23C', '#67C23A', '#409EFF', '#9C27B0'],
        tooltip: {
            trigger: 'axis',
            axisPointer: { type: 'cross', label: { backgroundColor: '#6a7985' } },
            backgroundColor: 'rgba(255, 255, 255, 0.95)',
            borderColor: '#e2e8f0',
            borderWidth: 1,
            padding: 12,
            textStyle: { color: '#333' },
            formatter: (params: any) => {
                const isPrediction = prediction.value && params[0]?.dataIndex === totalPoints - 1
                let header = `<div style="font-weight:600;margin-bottom:6px;">${params[0]?.axisValue || ''}`
                if (isPrediction) {
                    header += ' <span style="color:#E6A23C;font-size:11px;">(预测值)</span>'
                }
                header += '</div>'
                let body = ''
                for (const p of params) {
                    const val = typeof p.value === 'object' ? p.value.value : p.value
                    body += `<div style="display:flex;align-items:center;gap:6px;margin:3px 0;">
                        ${p.marker} <span>${p.seriesName}:</span> <strong>${val != null ? Number(val).toFixed(4) : '-'}</strong>
                    </div>`
                }
                return header + body
            }
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
                rotate: 20,
                fontSize: 12,
                color: '#666',
                margin: 15,
                formatter: (value: string, index: number) => {
                    if (prediction.value && index === totalPoints - 1) {
                        return '{predict|' + value + '}'
                    }
                    return value
                },
                rich: {
                    predict: {
                        color: '#E6A23C',
                        fontWeight: 'bold',
                        fontStyle: 'italic'
                    }
                }
            },
            axisLine: { lineStyle: { color: '#dcdfe6' } }
        },
        yAxis: {
            type: 'value',
            min: (value: any) => {
                return (Math.floor(value.min * 10) / 10 - 0.1).toFixed(1)
            },
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
            buildSeries('整体难度 (Difficulty)', difficultySeries, '#E6A23C', 'rgba(230,162,60,0.3)', 'rgba(230,162,60,0.2)'),
            buildSeries('区分度 (Discrimination)', discriminationSeries, '#67C23A', 'rgba(103,194,58,0.3)', 'rgba(103,194,58,0.2)'),
            buildSeries('信度 (Reliability)', reliabilitySeries, '#409EFF', 'rgba(64,158,255,0.3)', 'rgba(64,158,255,0.2)'),
            buildSeries('效度 (Validity)', validitySeries, '#9C27B0', 'rgba(156,39,176,0.3)', 'rgba(156,39,176,0.2)')
        ]
    }

    // Add prediction mark area (vertical band highlighting the prediction column)
    if (prediction.value) {
        option.series[0].markArea = {
            silent: true,
            itemStyle: {
                color: 'rgba(230, 162, 60, 0.06)',
                borderColor: 'rgba(230, 162, 60, 0.3)',
                borderWidth: 1,
                borderType: 'dashed'
            },
            data: [[
                { xAxis: xAxisLabels[totalPoints - 1] },
                { xAxis: xAxisLabels[totalPoints - 1] }
            ]],
            label: {
                show: true,
                position: 'insideTop',
                formatter: '预测区间',
                fontSize: 11,
                color: '#E6A23C'
            }
        }
    }

    return option
})
</script>

<style scoped>
.examination-paper-management {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header { margin-bottom: 24px; }
.page-header h2 { margin: 0; font-size: 24px; color: #303133; font-weight: 600; }
.subtitle { margin: 8px 0 0; color: #909399; font-size: 14px; }
.toolbar-card { margin-bottom: 16px; border-radius: 8px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 16px; }
.search-area { display: flex; gap: 12px; align-items: center; }
.search-input { width: 200px; }
.table-card { border-radius: 8px; }

.chart-wrapper {
    height: 500px;
    width: 100%;
}
.trend-chart {
    height: 100%;
    width: 100%;
}

/* Prediction Section */
.prediction-section {
    margin-top: 24px;
    padding: 20px;
    background: linear-gradient(135deg, #fafbfc 0%, #f0f5ff 100%);
    border-radius: 12px;
    border: 1px solid #e8ecf1;
}

.prediction-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    flex-wrap: wrap;
    gap: 12px;
}

.prediction-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    display: flex;
    align-items: center;
    gap: 8px;
}

.prediction-icon {
    font-size: 22px;
}

.prediction-meta {
    display: flex;
    align-items: center;
}

.prediction-cards {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
    margin-bottom: 20px;
}

@media (max-width: 900px) {
    .prediction-cards {
        grid-template-columns: repeat(2, 1fr);
    }
}

.prediction-card {
    background: #fff;
    border-radius: 10px;
    padding: 20px;
    text-align: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    border-top: 3px solid;
    transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.prediction-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.difficulty-card { border-top-color: #E6A23C; }
.discrimination-card { border-top-color: #67C23A; }
.reliability-card { border-top-color: #409EFF; }
.validity-card { border-top-color: #9C27B0; }

.indicator-label {
    font-size: 13px;
    color: #909399;
    margin-bottom: 8px;
    font-weight: 500;
}

.indicator-value {
    font-size: 32px;
    font-weight: 700;
    color: #303133;
    line-height: 1.2;
    margin-bottom: 8px;
}

.indicator-trend {
    font-size: 13px;
    margin-bottom: 6px;
    font-weight: 500;
    padding: 2px 8px;
    border-radius: 4px;
    display: inline-block;
}

.trend-up-good {
    color: #67C23A;
    background: rgba(103, 194, 58, 0.1);
}

.trend-up-warn {
    color: #E6A23C;
    background: rgba(230, 162, 60, 0.1);
}

.trend-down-warn {
    color: #F56C6C;
    background: rgba(245, 108, 108, 0.1);
}

.trend-stable {
    color: #909399;
    background: rgba(144, 147, 153, 0.1);
}

.indicator-range {
    font-size: 12px;
    color: #b0b3b8;
    font-family: 'Courier New', monospace;
}

.prediction-advice {
    margin-top: 4px;
}

.prediction-advice ul {
    margin: 5px 0 0 0;
    padding-left: 20px;
}

.prediction-advice li {
    margin-bottom: 5px;
    line-height: 1.6;
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
