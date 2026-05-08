<template>
  <el-drawer
    v-model="drawerVisible"
    :title="drawerTitle"
    size="50%"
    destroy-on-close
    @opened="handleOpened"
  >
    <div v-loading="loading" class="drawer-content">
      <el-alert
        v-if="questionId"
        :title="`正在过滤查看题目 ID: ${questionId} 的专属优化建议`"
        type="info"
        show-icon
        :closable="false"
        style="margin-bottom: 16px;"
      />
      
      <el-table :data="suggestions" stripe style="width: 100%" border>
        <el-table-column prop="questionId" label="题目ID" width="80" align="center" />
        <el-table-column prop="suggestionType" label="类型" width="120" align="center">
          <template #default="scope">
            <el-tag :type="getTypeTag(scope.row.suggestionType)">
              {{ getTypeName(scope.row.suggestionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="suggestionText" label="优化建议内容" min-width="250" />
        <el-table-column prop="isImplemented" label="状态" width="100" align="center">
          <template #default="scope">
            <el-switch
              v-model="scope.row.isImplemented"
              inline-prompt
              active-text="已采纳"
              inactive-text="待定"
              disabled
            />
          </template>
        </el-table-column>
      </el-table>
      
      <div v-if="!suggestions.length && !loading" class="empty-state">
        <el-empty description="暂无AI优化建议数据..." />
      </div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getSuggestionsByPaperId, type ImprovementSuggestion } from '../../api/analysis'

const drawerVisible = ref(false)
const loading = ref(false)
const paperId = ref<number | null>(null)
const questionId = ref<number | null>(null)
const suggestions = ref<ImprovementSuggestion[]>([])

const drawerTitle = computed(() => {
    return questionId.value 
        ? `试题优化建议` 
        : `试卷全局优化建议汇总（试卷加载排查中）`
})

const openDrawer = (pId: number, qId?: number) => {
    paperId.value = pId
    questionId.value = qId || null
    drawerVisible.value = true
}

defineExpose({ openDrawer })

const loadSuggestions = async () => {
    if (!paperId.value) return
    loading.value = true
    try {
        const res = await getSuggestionsByPaperId(paperId.value, questionId.value || undefined)
        suggestions.value = res.data || []
    } catch (error: any) {
        ElMessage.error(error.message || '获取优化建议失败')
    } finally {
        loading.value = false
    }
}

const handleOpened = () => {
    loadSuggestions()
}

const getTypeTag = (type: string) => {
    if (type === 'DIFFICULTY') return 'danger'
    if (type === 'DISCRIMINATION') return 'warning'
    if (type === 'CONTENT') return 'primary'
    return 'info'
}

const getTypeName = (type: string) => {
    if (type === 'DIFFICULTY') return '难度失衡'
    if (type === 'DISCRIMINATION') return '区分度低'
    if (type === 'CONTENT') return '内容优化'
    return type || '常规建议'
}
</script>

<style scoped>
.drawer-content {
    padding: 0 20px;
    height: 100%;
}
.empty-state {
    margin-top: 40px;
}
</style>
