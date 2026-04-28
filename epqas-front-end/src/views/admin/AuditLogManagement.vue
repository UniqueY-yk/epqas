<template>
  <div class="audit-log-management examination-paper-management">
    <div class="page-header">
      <h2>审计日志查询</h2>
      <p class="subtitle">追踪系统关键操作记录，支持按操作人、操作类型、时间范围筛选</p>
    </div>

    <!-- 搜索条件 -->
    <el-card class="toolbar-card" shadow="hover">
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="操作人ID">
          <el-input
            v-model="searchForm.userId"
            placeholder="用户ID"
            clearable
            style="width: 120px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.actionType" placeholder="全部" clearable style="width: 130px">
            <el-option label="新增" value="CREATE" />
            <el-option label="修改" value="UPDATE" />
            <el-option label="删除" value="DELETE" />
            <el-option label="查询" value="QUERY" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作模块">
          <el-input
            v-model="searchForm.targetTable"
            placeholder="模糊搜索..."
            clearable
            style="width: 160px"
          />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 360px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="RefreshRight" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="hover" class="table-card">
      <el-table :data="tableData" style="width: 100%" v-loading="loading" stripe border>
        <template #empty>
          <el-empty description="暂无审计日志" />
        </template>
        <el-table-column prop="logId" label="ID" width="70" align="center" />
        <el-table-column prop="userId" label="操作人ID" width="90" align="center" />
        <el-table-column prop="actionType" label="操作类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getActionTypeTag(row.actionType)" size="small">
              {{ formatActionType(row.actionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetTable" label="操作模块" min-width="160" show-overflow-tooltip />
        <el-table-column prop="requestMethod" label="请求方式" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getMethodTag(row.requestMethod)" size="small" effect="plain">
              {{ row.requestMethod }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requestUrl" label="请求URL" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ipAddress" label="IP地址" width="130" />
        <el-table-column prop="duration" label="耗时(ms)" width="90" align="center">
          <template #default="{ row }">
            <span :class="{ 'duration-slow': row.duration > 1000 }">{{ row.duration ?? '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
              {{ row.status === 0 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="actionTime" label="操作时间" width="170" />
        <el-table-column label="详情" width="70" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showDetail(row)">
              <el-icon><View /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          :current-page="currentPage"
          :page-size="pageSize"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 日志详情对话框 -->
    <el-dialog v-model="detailVisible" title="审计日志详情" width="650px" destroy-on-close>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="日志ID">{{ detailRow.logId }}</el-descriptions-item>
        <el-descriptions-item label="操作人ID">{{ detailRow.userId ?? '未知' }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ formatActionType(detailRow.actionType) }}</el-descriptions-item>
        <el-descriptions-item label="操作状态">
          <el-tag :type="detailRow.status === 0 ? 'success' : 'danger'" size="small">
            {{ detailRow.status === 0 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作模块" :span="2">{{ detailRow.targetTable }}</el-descriptions-item>
        <el-descriptions-item label="请求方式">{{ detailRow.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="请求URL">{{ detailRow.requestUrl }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ detailRow.ipAddress }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ detailRow.duration ? detailRow.duration + ' ms' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ detailRow.actionTime }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <el-input
            v-if="detailRow.requestParams"
            type="textarea"
            :model-value="detailRow.requestParams"
            :rows="4"
            readonly
          />
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailRow.errorMsg" label="错误信息" :span="2">
          <el-text type="danger">{{ detailRow.errorMsg }}</el-text>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getAuditLogPage } from '@/api/audit'
import { Search, RefreshRight, View } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 搜索表单
const searchForm = reactive({
  userId: '',
  actionType: '',
  targetTable: '',
  timeRange: null as string[] | null
})

// 详情对话框
const detailVisible = ref(false)
const detailRow = ref<any>({})

/**
 * 获取审计日志分页数据
 */
const fetchData = async () => {
  loading.value = true
  try {
    const params: any = {
      current: currentPage.value,
      size: pageSize.value
    }
    if (searchForm.userId) params.userId = searchForm.userId
    if (searchForm.actionType) params.actionType = searchForm.actionType
    if (searchForm.targetTable) params.targetTable = searchForm.targetTable
    if (searchForm.timeRange && searchForm.timeRange.length === 2) {
      params.startTime = searchForm.timeRange[0]
      params.endTime = searchForm.timeRange[1]
    }
    const res = await getAuditLogPage(params)
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.error('获取审计日志失败', e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchData()
}

const handleReset = () => {
  searchForm.userId = ''
  searchForm.actionType = ''
  searchForm.targetTable = ''
  searchForm.timeRange = null
  currentPage.value = 1
  fetchData()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchData()
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
  fetchData()
}

const showDetail = (row: any) => {
  detailRow.value = row
  detailVisible.value = true
}

/**
 * 格式化操作类型
 */
const formatActionType = (type: string) => {
  const map: Record<string, string> = {
    'CREATE': '新增',
    'UPDATE': '修改',
    'DELETE': '删除',
    'QUERY': '查询',
    'OTHER': '其他'
  }
  return map[type] || type || '-'
}

/**
 * 获取操作类型对应的 Tag 颜色
 */
const getActionTypeTag = (type: string) => {
  const map: Record<string, string> = {
    'CREATE': 'success',
    'UPDATE': 'warning',
    'DELETE': 'danger',
    'QUERY': 'info',
    'OTHER': ''
  }
  return map[type] || 'info'
}

/**
 * 获取请求方式对应的 Tag 颜色
 */
const getMethodTag = (method: string) => {
  const map: Record<string, string> = {
    'GET': 'info',
    'POST': 'success',
    'PUT': 'warning',
    'DELETE': 'danger'
  }
  return map[method] || ''
}

onMounted(() => {
  fetchData()
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
.search-form { display: flex; flex-wrap: wrap; gap: 4px; align-items: flex-end; }
.table-card { border-radius: 8px; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
.duration-slow { color: #f56c6c; font-weight: 600; }
</style>
