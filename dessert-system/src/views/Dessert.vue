<template>
  <div class="container">
    <h2>🍮 甜点管理</h2>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchForm.name"
        placeholder="甜点名称"
        style="width: 200px; margin-right: 10px"
        clearable
        @keyup.enter="handleQuery"
      />
      <el-select
        v-model="searchForm.catId"
        placeholder="分类筛选"
        clearable
        style="width: 150px; margin-right: 10px"
      >
        <el-option
          v-for="item in categories"
          :key="item.id"
          :label="item.name"
          :value="item.id"
        />
      </el-select>
      <el-button type="primary" @click="handleQuery">搜索</el-button>
      <el-button @click="resetQuery">重置</el-button>
    </div>

    <!-- 操作按钮 -->
    <div class="actions" style="margin: 20px 0">
      <el-button type="success" @click="openAddDialog">新增甜点</el-button>
      <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">
        批量删除
      </el-button>
    </div>

    <!-- 数据表格 -->
    <el-table
      :data="dessertList"
      border
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="甜点名称" />
      <el-table-column label="图片" width="120">
        <template #default="scope">
          <img
            v-if="scope.row.photoUrl"
            :src="getImageUrl(scope.row.photoUrl)"
            alt="甜点图片"
            style="width: 60px; height: 60px; border-radius: 4px; object-fit: cover"
          />
          <span v-else>无图片</span>
        </template>
      </el-table-column>
      <el-table-column prop="price" label="价格" width="100">
        <template #default="scope">
          ¥{{ scope.row.price }}
        </template>
      </el-table-column>
      <el-table-column prop="categoryName" label="分类" width="120" />

      <!-- 推荐列 -->
      <el-table-column label="推荐" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.isRecommend === 1" type="success">推荐</el-tag>
          <span v-else>普通</span>
        </template>
      </el-table-column>

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
    <div class="pagination" style="margin-top: 20px; text-align: right">
      <el-pagination
        background
        layout="prev, pager, next, total"
        :total="total"
        :page-size="searchForm.pageSize"
        :current-page="searchForm.pageNum"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="550px"
      @close="resetForm"
    >
      <el-form
        :model="formData"
        :rules="rules"
        ref="dessertFormRef"
        label-width="80px"
      >
        <el-form-item label="甜点名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入名称" />
        </el-form-item>

        <el-form-item label="价格" prop="price">
          <el-input-number
            v-model="formData.price"
            :min="0"
            :precision="2"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="分类" prop="catId">
          <el-select v-model="formData.catId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="item in categories"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <!-- 图片上传组件 -->
        <el-form-item label="商品图片" prop="photoUrl">
          <div class="upload-container">
            <el-upload
              class="avatar-uploader"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              :before-upload="beforeUpload"
              name="file"
            >
              <img v-if="formData.photoUrl" :src="getImageUrl(formData.photoUrl)" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">
              点击上传图片（支持 jpg、png、gif，大小不超过 5MB）
            </div>
          </div>
        </el-form-item>

        <!-- 推荐选项 -->
        <el-form-item label="推荐" prop="isRecommend">
          <el-radio-group v-model="formData.isRecommend">
            <el-radio :label="1">是</el-radio>
            <el-radio :label="0">否</el-radio>
          </el-radio-group>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules, UploadProps } from 'element-plus'
import request from '@/api/request'
import { message } from '@/utils/message'

// 类型定义
interface Dessert {
  id: number
  name: string
  price: number
  photoUrl: string
  catId: number
  isRecommend: number
  categoryName?: string
  status?: number
  stock?: number
  descp?: string
}

interface Category {
  id: number
  name: string
  descp?: string
}

// 响应数据类型 - TableDataInfo
interface TableDataInfo {
  total: number
  rows: Dessert[]
  code: number
  msg: string
  timestamp: number
}

// 后端基础地址
const baseUrl = 'http://localhost:8080'

// 数据状态
const dessertList = ref<Dessert[]>([])
const categories = ref<Category[]>([])
const total = ref(0)
const selectedIds = ref<number[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增甜点')
const dessertFormRef = ref<FormInstance>()
const submitting = ref(false)

// 搜索参数
const searchForm = reactive({
  pageNum: 1,
  pageSize: 10,
  name: '',
  catId: null as number | null,
})

// 表单数据
const formData = reactive({
  id: 0,
  name: '',
  price: 0,
  photoUrl: '',
  catId: 0,
  isRecommend: 0,
})

// 表单验证规则
const rules: FormRules = {
  name: [{ required: true, message: '请输入甜点名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  catId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  photoUrl: [{ required: true, message: '请上传图片', trigger: 'change' }],
}

// 上传配置 - 修复404错误：使用 /api 前缀
const uploadUrl = '/api/dessert/upload'

// 上传请求头（携带token）
const uploadHeaders = computed(() => ({
  token: localStorage.getItem('token') || ''
}))

// 获取图片完整URL
const getImageUrl = (url: string) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return `${baseUrl}${url}`
}

// 上传前校验
const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/gif'
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    message.error('只能上传 JPG/PNG/GIF 格式的图片!')
    return false
  }
  if (!isLt5M) {
    message.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

// 上传成功回调
const handleUploadSuccess = (response: any, file: any) => {
  console.log('上传成功:', response)

  if (response.code === 200 && response.data) {
    formData.photoUrl = response.data
    message.success('图片上传成功')
  } else if (response.data) {
    formData.photoUrl = response.data
    message.success('图片上传成功')
  } else {
    message.error('上传失败，请重试')
  }
}

// 上传失败回调
const handleUploadError = (error: any) => {
  console.error('上传失败:', error)
  message.error('图片上传失败，请重试')
}

// 获取甜点列表
const fetchData = async () => {
  try {
    const params: Record<string, any> = {
      pageNum: searchForm.pageNum,
      pageSize: searchForm.pageSize
    }

    if (searchForm.name) {
      params.name = searchForm.name
    }
    if (searchForm.catId) {
      params.catId = searchForm.catId
    }

    const response = await request.get<TableDataInfo>('/dessert/list', { params })
    const data = response.data || response

    dessertList.value = data.rows || []
    total.value = data.total || 0
  } catch (error) {
    console.error('获取数据失败:', error)
    message.error('获取数据失败')
  }
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    const response = await request.get('/category', {
      params: {
        pageNum: 1,
        pageSize: 100
      }
    })

    const data = response.data || response

    if (data.rows && Array.isArray(data.rows)) {
      categories.value = data.rows
    } else if (Array.isArray(data)) {
      categories.value = data
    } else {
      categories.value = []
    }

    console.log('获取分类成功，数量:', categories.value.length)
  } catch (error) {
    console.error('获取分类失败:', error)
    categories.value = []
    message.warning('获取分类失败')
  }
}

// 搜索与重置
const handleQuery = () => {
  searchForm.pageNum = 1
  fetchData()
}

// 分页改变
const handlePageChange = (page: number) => {
  searchForm.pageNum = page
  fetchData()
}

const resetQuery = () => {
  searchForm.name = ''
  searchForm.catId = null
  searchForm.pageNum = 1
  fetchData()
}

// 选择变化
const handleSelectionChange = (val: Dessert[]) => {
  selectedIds.value = val.map((item) => item.id)
}

// 打开新增弹窗
const openAddDialog = () => {
  dialogTitle.value = '新增甜点'
  formData.id = 0
  formData.name = ''
  formData.price = 0
  formData.photoUrl = ''
  formData.catId = 0
  formData.isRecommend = 0
  dialogVisible.value = true
}

// 打开编辑弹窗
const openEditDialog = (row: Dessert) => {
  dialogTitle.value = '编辑甜点'
  formData.id = row.id
  formData.name = row.name
  formData.price = row.price
  formData.photoUrl = row.photoUrl
  formData.catId = row.catId
  formData.isRecommend = row.isRecommend
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  dessertFormRef.value?.resetFields()
}

// 提交表单
const submitForm = async () => {
  if (!dessertFormRef.value) return

  try {
    await dessertFormRef.value.validate()
    submitting.value = true

    // 构建完整的数据对象，匹配数据库字段
    const submitData = {
      id: formData.id,
      name: formData.name,
      descp: formData.name,                    // 描述使用名称
      photoUrl: formData.photoUrl,
      price: formData.price,
      originalPrice: formData.price,           // 原价与现价相同
      discount: 1.0,                           // 折扣1.0
      releaseDate: new Date().toISOString().split('T')[0], // 发布日期
      catId: formData.catId,
      stock: 999,                              // 默认库存
      minStock: 5,                             // 最低库存
      status: 1,                               // 状态：上架
      isRecommend: formData.isRecommend
    }

    console.log('提交数据:', submitData)

    if (dialogTitle.value === '新增甜点') {
      await request.post('/dessert', submitData)
      message.success('新增成功')
    } else {
      await request.put('/dessert', submitData)
      message.success('更新成功')
    }
    dialogVisible.value = false
    await fetchData()
  } catch (error: any) {
    console.error('操作失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '操作失败'
    message.error(errorMsg)
  } finally {
    submitting.value = false
  }
}

// 删除单条
const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定删除该甜点吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await request.delete(`/dessert/${id}`)
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

  ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 条数据吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      for (const id of selectedIds.value) {
        await request.delete(`/dessert/${id}`)
      }
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
  fetchCategories()
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

/* 图片上传样式 */
.upload-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.avatar-uploader {
  width: 120px;
  height: 120px;
}

.avatar-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fafafa;
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: #409eff;
  background-color: #f5f7fa;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  text-align: center;
  line-height: 120px !important;
}

.avatar {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  line-height: 1.5;
}
</style>
