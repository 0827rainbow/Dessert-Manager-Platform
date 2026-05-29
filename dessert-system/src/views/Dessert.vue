<template>
  <div class="container">
    <h2>🍮 甜点管理</h2>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="searchForm.name" placeholder="甜点名称" style="width: 200px; margin-right: 10px" clearable
        @keyup.enter="handleQuery" />
      <el-select v-model="searchForm.catId" placeholder="分类筛选" clearable style="width: 150px; margin-right: 10px">
        <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
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
    <el-table :data="dessertList" border style="width: 100%" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="甜点名称" />
      <el-table-column label="图片" width="120">
        <template #default="scope">
          <img v-if="scope.row.photoUrl" :src="getImageUrl(scope.row.photoUrl)" alt="甜点图片"
            style="width: 60px; height: 60px; border-radius: 4px; object-fit: cover" />
          <span v-else>无图片</span>
        </template>
      </el-table-column>
      <el-table-column prop="price" label="价格" width="100">
        <template #default="scope">
          ¥{{ scope.row.price }}
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="100" />
      <el-table-column prop="categoryName" label="分类" width="120" />
      <el-table-column label="推荐" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.isRecommend === 1" type="success">推荐</el-tag>
          <span v-else>普通</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button size="small" type="primary" @click="openEditDialog(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination" style="margin-top: 20px; text-align: right">
      <el-pagination background layout="prev, pager, next, total" :total="total" :page-size="searchForm.pageSize"
        :current-page="searchForm.pageNum" @current-change="handlePageChange" />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="550px" @close="resetForm">
      <el-form :model="formData" :rules="rules" ref="dessertFormRef" label-width="80px">
        <el-form-item label="甜点名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入名称" />
        </el-form-item>

        <el-form-item label="甜点描述" prop="descp">
          <el-input v-model="formData.descp" type="textarea" rows="3" placeholder="请输入甜点描述（例如：口感、用料、风味等）"
            maxlength="500" show-word-limit />
        </el-form-item>

        <el-form-item label="价格" prop="price">
          <el-input-number v-model="formData.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>

        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="formData.stock" :min="0" :step="1" style="width: 100%" />
        </el-form-item>

        <el-form-item label="分类" prop="catId">
          <el-select v-model="formData.catId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>

        <!-- 图片上传组件 -->
        <el-form-item label="商品图片" prop="photoUrl">
          <div class="upload-container">
            <el-upload class="avatar-uploader" :action="uploadUrl" :headers="uploadHeaders" :show-file-list="false"
              :on-success="handleUploadSuccess" :on-error="handleUploadError" :before-upload="beforeUpload" name="file">
              <img v-if="formData.photoUrl" :src="getImageUrl(formData.photoUrl)" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon">
                <Plus />
              </el-icon>
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
  descp: string           // 新增描述字段
  price: number
  stock: number
  photoUrl: string
  catId: number
  isRecommend: number
  categoryName?: string
  status?: number
}

interface Category {
  id: number
  name: string
  descp?: string
}

// 响应数据类型
interface TableDataInfo {
  total: number
  rows: Dessert[]
  code: number
  msg: string
  timestamp: number
}

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

// 表单数据（增加 descp 字段）
const formData = reactive({
  id: 0,
  name: '',
  descp: '',              // 甜点描述
  price: 0,
  stock: 0,
  photoUrl: '',
  catId: 0,
  isRecommend: 0,
})

// 表单验证规则（增加 descp 可选，但可加长度限制）
const rules: FormRules = {
  name: [{ required: true, message: '请输入甜点名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [
    { required: true, message: '请输入库存', trigger: 'blur' },
    { type: 'number', min: 0, message: '库存必须为大于等于0的数字', trigger: 'blur' }
  ],
  catId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  photoUrl: [{ required: true, message: '请上传图片', trigger: 'change' }],
  // descp 可选，不做必填验证
}

const uploadUrl = '/api/dessert/upload'
const uploadHeaders = computed(() => ({
  token: localStorage.getItem('token') || ''
}))

const getImageUrl = (url: string) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return `${baseUrl}${url}`
}

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

const handleUploadSuccess = (response: any) => {
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

const handleUploadError = () => {
  message.error('图片上传失败，请重试')
}

// 获取甜点列表
const fetchData = async () => {
  try {
    const params: Record<string, any> = {
      pageNum: searchForm.pageNum,
      pageSize: searchForm.pageSize
    }
    if (searchForm.name) params.name = searchForm.name
    if (searchForm.catId) params.catId = searchForm.catId

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
      params: { pageNum: 1, pageSize: 100 }
    })
    const data = response.data || response
    categories.value = data.rows || []
  } catch (error) {
    console.error('获取分类失败:', error)
    categories.value = []
    message.warning('获取分类失败')
  }
}

const handleQuery = () => {
  searchForm.pageNum = 1
  fetchData()
}

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

const handleSelectionChange = (val: Dessert[]) => {
  selectedIds.value = val.map((item) => item.id)
}

// 打开新增弹窗（重置 descp）
const openAddDialog = () => {
  dialogTitle.value = '新增甜点'
  formData.id = 0
  formData.name = ''
  formData.descp = ''
  formData.price = 0
  formData.stock = 0
  formData.photoUrl = ''
  formData.catId = 0
  formData.isRecommend = 0
  dialogVisible.value = true
}

// 打开编辑弹窗（读取 descp）
const openEditDialog = (row: Dessert) => {
  dialogTitle.value = '编辑甜点'
  formData.id = row.id
  formData.name = row.name
  formData.descp = row.descp || ''
  formData.price = row.price
  formData.stock = row.stock ?? 0
  formData.photoUrl = row.photoUrl
  formData.catId = row.catId
  formData.isRecommend = row.isRecommend
  dialogVisible.value = true
}

const resetForm = () => {
  dessertFormRef.value?.resetFields()
}

// 提交表单（提交 descp）
const submitForm = async () => {
  if (!dessertFormRef.value) return
  try {
    await dessertFormRef.value.validate()
    submitting.value = true

    const submitData = {
      id: formData.id,
      name: formData.name,
      descp: formData.descp,               // 使用用户填写的描述
      photoUrl: formData.photoUrl,
      price: formData.price,
      originalPrice: formData.price,
      discount: 1.0,
      releaseDate: new Date().toISOString().split('T')[0],
      catId: formData.catId,
      stock: formData.stock,
      minStock: 5,
      status: 1,
      isRecommend: formData.isRecommend
    }

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
    } catch {
      message.error('删除失败')
    }
  }).catch(() => { })
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
    } catch {
      message.error('批量删除失败')
    }
  }).catch(() => { })
}

onMounted(() => {
  fetchData()
  fetchCategories()
})
</script>

<style scoped>
/* 原有样式保持不变 */
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
