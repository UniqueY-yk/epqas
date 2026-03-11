<template>
  <el-dialog
    v-model="dialogVisible"
    :title="`题目质量异动监控 - ${examTitle}`"
    width="800px"
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
      />
    </div>
    
    <div class="legend-info">
      <p>💡 <b>分析说明:</b></p>
      <ul>
        <li><span class="dot normal"></span> <b>绿色点:</b> 质量达标的正常题目。</li>
        <li><span class="dot abnormal"></span> <b>红色点:</b> 质量异常题目（如区分度过低或难度极其失衡），建议命题人重点关注并修正。</li>
        <li><b>横坐标(X):</b> 难度水平 (答对率) - 值越高代表题目越简单。</li>
        <li><b>纵坐标(Y):</b> 区分度 - 值越高代表题目越能区分高低分考生。</li>
      </ul>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">关闭窗口</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { use } from 'echarts/core'
import { ScatterChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, GridComponent, DatasetComponent, GraphicComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import VChart from 'vue-echarts'
import { getQuestionAnalysisByExamId, type QuestionAnalysisVO } from '../../api/analysis'

// Register ECharts core components
use([
  TitleComponent,
  TooltipComponent,
  GridComponent,
  DatasetComponent,
  ScatterChart,
  CanvasRenderer,
  GraphicComponent
])

const dialogVisible = ref(false)
const loading = ref(false)
const examId = ref<number>(0)
const examTitle = ref<string>('')
const chartData = ref<QuestionAnalysisVO[]>([])

// Expose open method to parent component
const openDialog = (id: number, title: string) => {
  examId.value = id
  examTitle.value = title
  dialogVisible.value = true
}

defineExpose({ openDialog })

const handleOpened = async () => {
    loading.value = true
    try {
        const res = await getQuestionAnalysisByExamId(examId.value)
        chartData.value = res.data || []
    } catch (e: any) {
        ElMessage.error(e.message || '获取分析数据失败')
    } finally {
        loading.value = false
    }
}

// Compute ECharts Option
const chartOption = computed(() => {
    return {
        title: {
            text: '试卷试题分布散点图',
            left: 'center',
            textStyle: { fontSize: 16 }
        },
        tooltip: {
            // Display rich HTML tag for scatter plot dots
            formatter: function (params: any) {
                const data: QuestionAnalysisVO = params.data.rawData;
                return `
                    <div style="max-width:300px; white-space:normal;">
                        <b>[题目${data.questionId}]</b> <br/>
                        ${data.stem || '暂无题目内容'} <br/>
                        <hr style="margin: 5px 0; border: 0; border-top: 1px solid #ccc;" />
                        难度 (答对率): ${data.correctResponseRate?.toFixed(2)} <br/>
                        区分度: ${data.discriminationIndex?.toFixed(2)} <br/>
                        状态: <strong style="color:${data.isAbnormal ? '#F56C6C' : '#67C23A'}">
                                ${data.isAbnormal ? '异动警告' : '达标'}
                              </strong>
                    </div>
                `
            }
        },
        grid: {
            left: 50,
            right: 50,
            bottom: 50,
            top: 60
        },
        xAxis: {
            type: 'value',
            name: '难度 (答对率)',
            nameLocation: 'middle',
            nameGap: 30,
            min: 0,
            max: 1.0,
            interval: 0.2
        },
        yAxis: {
            type: 'value',
            name: '区分度',
            nameLocation: 'middle',
            nameGap: 30,
            min: -0.5,
            max: 1.0,
            interval: 0.2
        },
        series: [
            {
                type: 'scatter',
                symbolSize: 12,
                itemStyle: {
                    // Color based on abnormality
                    color: function(params: any) {
                        return params.data.rawData.isAbnormal ? '#F56C6C' : '#67C23A';
                    },
                    opacity: 0.8
                },
                data: chartData.value.map((item: QuestionAnalysisVO) => ({
                    // ECharts Scatter requires Array [x, y]
                    value: [item.correctResponseRate, item.discriminationIndex],
                    rawData: item // Store full object for tooltip usage
                }))
            }
        ]
    }
})
</script>

<style scoped>
.chart-container {
    height: 400px;
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

.legend-info {
    margin-top: 20px;
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
