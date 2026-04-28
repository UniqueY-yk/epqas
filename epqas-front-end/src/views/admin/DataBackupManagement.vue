<template>
  <div class="data-backup-management examination-paper-management">
    <div class="page-header">
      <h2>数据备份管理</h2>
      <p class="subtitle">管理系统数据库备份，支持手动创建和删除备份文件</p>
    </div>

    <!-- 操作栏 -->
    <el-card class="toolbar-card" shadow="hover">
      <div class="toolbar">
        <div class="action-area">
          <el-button type="primary" :icon="FolderAdd" :loading="creating" @click="handleCreate">
            创建备份
          </el-button>
          <el-button :icon="Refresh" @click="fetchBackups">刷新列表</el-button>
        </div>
        <div class="backup-tip">
          <el-icon color="#909399"><InfoFilled /></el-icon>
          <span class="tip-text">备份文件存储于服务器 <code>./backups</code> 目录</span>
        </div>
      </div>
    </el-card>

    <!-- 备份文件列表 -->
    <el-card shadow="hover" class="table-card">
      <el-table :data="backupList" style="width: 100%" v-loading="loading" stripe border>
        <template #empty>
          <el-empty description="暂无备份文件，点击上方按钮创建" />
        </template>
        <el-table-column type="index" label="#" width="60" align="center" />
        <el-table-column prop="fileName" label="文件名" min-width="280">
          <template #default="{ row }">
            <el-icon color="#409EFF" style="margin-right: 6px; vertical-align: middle;"><Document /></el-icon>
            <span>{{ row.fileName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="fileSizeReadable" label="文件大小" width="120" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              type="danger"
              size="small"
              plain
              :icon="Delete"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getBackupList, createBackup, deleteBackup } from '@/api/backup'
import { ElMessage, ElMessageBox } from 'element-plus'
import { FolderAdd, Refresh, Delete, Document, InfoFilled } from '@element-plus/icons-vue'

const loading = ref(false)
const creating = ref(false)
const backupList = ref<any[]>([])

/**
 * 获取备份文件列表
 */
const fetchBackups = async () => {
  loading.value = true
  try {
    const res = await getBackupList()
    backupList.value = res.data || []
  } catch (e) {
    console.error('获取备份列表失败', e)
  } finally {
    loading.value = false
  }
}

/**
 * 创建数据库备份（带二次确认）
 */
const handleCreate = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要创建数据库备份吗？备份过程可能需要几秒钟。',
      '创建备份确认',
      {
        confirmButtonText: '确定创建',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    creating.value = true
    const res = await createBackup()
    ElMessage.success(`备份创建成功: ${res.data}`)
    fetchBackups()
  } catch (e: any) {
    if (e !== 'cancel' && e?.message !== 'cancel') {
      ElMessage.error('备份创建失败')
    }
  } finally {
    creating.value = false
  }
}

/**
 * 删除备份文件（带二次确认）
 */
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除备份文件 "${row.fileName}" 吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await deleteBackup(row.fileName)
    ElMessage.success('备份文件已删除')
    fetchBackups()
  } catch (e: any) {
    if (e !== 'cancel' && e?.message !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchBackups()
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
.action-area { display: flex; gap: 12px; }
.backup-tip { display: flex; align-items: center; gap: 6px; }
.tip-text { color: #909399; font-size: 13px; }
.tip-text code {
  background: #f0f2f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
  font-size: 12px;
  color: #606266;
}
.table-card { border-radius: 8px; }
</style>
