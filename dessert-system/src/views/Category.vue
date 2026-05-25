<template>
  <div class="container">
    <h2>📁 分类管理</h2>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchForm.name"
        placeholder="分类名称"
        style="width: 200px; margin-right: 10px"
        clearable
        @keyup.enter="handleSearch"
      />
      <el-input
        v-model="searchForm.descp"
        placeholder="分类描述"
        style="width: 200px; margin-right: 10px"
        clearable
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <!-- 操作按钮 -->
    <div class="actions">
      <el-button type="success" @click="openAddDialog">新增分类</el-button>
      <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">
        批量删除
      </el-button>
    </div>

    <!-- 数据表格 -->
    <el-table
      :data="categoryList"
      border
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="分类名称" />
      <el-table-column prop="descp" label="分类描述" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button size="small" type="primary" @click="openEditDialog(scope.row)">编辑</el-button>
          <el-button
            size="small"
            type="danger"
            @click="handleDelete(scope.row.id)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        background
        layout="prev, pager, next, total"
        :total="total"
        :page-size="pageSize"
        :current-page="pageNum"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="500px"
      @close="resetForm"
    >
      <el-form
        :model="formData"
        :rules="rules"
        ref="categoryFormRef"
        label-width="80px"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类描述" prop="descp">
          <el-input
            v-model="formData.descp"
            type="textarea"
            rows="3"
            placeholder="请输入分类描述"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessageBox } from 'element-plus'
import request from '@/api/request'
import { message } from '@/utils/message'

// 类型定义
interface Category {
  id: number
  name: string
  descp: string
}

// 数据状态
const categoryList = ref<Category[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const selectedIds = ref<number[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增分类')
const categoryFormRef = ref()
const submitting = ref(false)

// 搜索表单
const searchForm = reactive({
  name: '',
  descp: ''
})

// 表单数据
const formData = reactive({
  id: 0,
  name: '',
  descp: ''
})

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

// 获取分类列表
const fetchData = async () => {
  try {
    const params: Record<string, any> = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }

    // 添加搜索参数
    if (searchForm.name) {
      params.name = searchForm.name
    }
    if (searchForm.descp) {
      params.descp = searchForm.descp
    }

    const response = await request.get('/category', { params })
    const data = response.data || response
    categoryList.value = data.rows || []
    total.value = data.total || 0
  } catch (error) {
    console.error('获取数据失败:', error)
    message.error('获取数据失败')
  }
}

// 搜索
const handleSearch = () => {
  pageNum.value = 1
  fetchData()
}

// 重置搜索
const resetSearch = () => {
  searchForm.name = ''
  searchForm.descp = ''
  pageNum.value = 1
  fetchData()
}

// 分页改变
const handlePageChange = (page: number) => {
  pageNum.value = page
  fetchData()
}

// 选择变化
const handleSelectionChange = (val: Category[]) => {
  selectedIds.value = val.map((item) => item.id)
}

// 打开新增弹窗
const openAddDialog = () => {
  dialogTitle.value = '新增分类'
  formData.id = 0
  formData.name = ''
  formData.descp = ''
  dialogVisible.value = true
}

// 打开编辑弹窗
const openEditDialog = (row: Category) => {
  dialogTitle.value = '编辑分类'
  formData.id = row.id
  formData.name = row.name
  formData.descp = row.descp || ''
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  categoryFormRef.value?.resetFields()
}

// 提交表单
const submitForm = async () => {
  if (!categoryFormRef.value) return

  try {
    await categoryFormRef.value.validate()
    submitting.value = true

    const submitData = {
      id: formData.id,
      name: formData.name,
      descp: formData.descp
    }

    if (dialogTitle.value === '新增分类') {
      await request.post('/category', submitData)
      message.success('新增成功')
    } else {
      await request.put('/category', submitData)
      message.success('更新成功')
    }
    dialogVisible.value = false
    await fetchData()
  } catch (error) {
    console.error('操作失败:', error)
    message.error('操作失败')
  } finally {
    submitting.value = false
  }
}

// 删除单条
const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定删除该分类吗？删除后可能影响相关甜点。', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await request.delete(`/category/${id}`)
      message.success('删除成功')
      await fetchData()
    } catch (error) {
      console.error('删除失败:', error)
      message.error('删除失败')
    }
  }).catch(() => {})
}

// 批量删除
const handleBatchDelete = () => {
  if (selectedIds.value.length === 0) return

  ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 条分类吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      const ids = selectedIds.value.join(',')
      await request.delete(`/category/${ids}`)
      message.success('批量删除成功')
      selectedIds.value = []
      await fetchData()
    } catch (error) {
      console.error('批量删除失败:', error)
      message.error('批量删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.container {
  padding: 20px;
}

h2 {
  margin-bottom: 20px;
  color: #333;
}

.search-bar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.actions {
  margin: 20px 0;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
