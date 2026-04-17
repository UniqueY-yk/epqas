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
import { getPropositionTrend, type PaperAnalysisVO } from '../../api/analysis'
import { getSetters, type SetterInfo } from '../../api/exam'
import { getCourses } from '../../api/academic'

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

const loadTrendData = async () => {
    // 必须同时选择命题教师和课程
    if (!canLoadTrend.value) {
        trendData.value = []
        return
    }

    loading.value = true
    try {
        if (!isAdmin) {
            // Setter: fetch only their own trend
            const userIdStr = localStorage.getItem('userId')
            const setterId = userIdStr ? Number(userIdStr) : 0

            if (!setterId) {
                ElMessage.error('无法获取当前命题教师身份信息')
                return
            }

            const res = await getPropositionTrend(setterId, selectedCourseId.value)
            trendData.value = res.data || []
        } else {
            // Admin: fetch selected setter's trend
            const res = await getPropositionTrend(selectedSetterId.value, selectedCourseId.value)
            trendData.value = res.data || []
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
            min: (value: any) => {
                // Ensure data points don't hit the absolute bottom margin
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
