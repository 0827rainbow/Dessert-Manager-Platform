<template>
  <div class="container">
    <h2>甜点管理</h2>
    <div class="toolbar">
      <el-button type="primary" @click="openAddDialog">新增甜点</el-button>
      <el-input v-model="searchName" placeholder="搜索甜点" style="width: 200px; margin-left: 10px" @keyup.enter="fetchList" />
      <el-button @click="fetchList">搜索</el-button>
    </div>
    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="图片" width="80">
        <template #default="{ row }">
          <img v-if="row.photoUrl" :src="baseUrl + row.photoUrl" style="width: 50px; height: 50px; object-fit: cover" />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="price" label="价格" width="100" />
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="openEditDialog(row)">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination
        :current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="甜点名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="分类" prop="catId">
          <el-select v-model="form.catId" placeholder="请选择分类">
            <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input v-model="form.price" type="number" />
        </el-form-item>
        <el-form-item label="原价">
          <el-input v-model="form.originalPrice" type="number" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input v-model="form.stock" type="number" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="推荐">
          <el-radio-group v-model="form.isRecommend">
            <el-radio :value="1">是</el-radio>
            <el-radio :value="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="图片">
          <el-upload :action="uploadUrl" :on-success="handleUploadSuccess" :before-upload="beforeUpload" :show-file-list="false">
            <el-button>上传图片</el-button>
          </el-upload>
          <img v-if="form.photoUrl" :src="baseUrl + form.photoUrl" style="width: 80px; margin-top: 10px" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.descp" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { dessertApi } from '@/api/dessert'
import { CategoryService, type Category } from '@/api/CategoryService'

interface Dessert {
  id: number
  name: string
  descp: string
  photoUrl: string
  price: number
  originalPrice: number
  stock: number
  status: number
  isRecommend: number
  catId: number
  categoryName: string
}

const baseUrl = 'http://localhost:8080'
const uploadUrl = baseUrl + '/dessert/upload'

const list = ref<Dessert[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const searchName = ref('')
const categories = ref<Category[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增甜点')
const isEdit = ref(false)
const formRef = ref()
const form = ref<Dessert>({
  id: 0, name: '', descp: '', photoUrl: '', price: 0, originalPrice: 0,
  stock: 0, status: 1, isRecommend: 0, catId: 0, categoryName: ''
})

const rules = {
  name: [{ required: true, message: '请输入甜点名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

const fetchCategories = async () => {
  try {
    const res: any = await CategoryService.list({ pageNum: 1, pageSize: 100 })
    categories.value = res.rows || []
  } catch (error) {
    console.error('获取分类失败', error)
  }
}

const fetchList = async () => {
  loading.value = true
  try {
    const res: any = await dessertApi.getList({ pageNum: pageNum.value, pageSize: pageSize.value, name: searchName.value })
    list.value = res.rows || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = async (page: number) => {
  pageNum.value = page
  await fetchList()
}

const openAddDialog = () => {
  isEdit.value = false
  dialogTitle.value = '新增甜点'
  form.value = { id: 0, name: '', descp: '', photoUrl: '', price: 0, originalPrice: 0, stock: 0, status: 1, isRecommend: 0, catId: 0, categoryName: '' }
  dialogVisible.value = true
}

const openEditDialog = (row: Dessert) => {
  isEdit.value = true
  dialogTitle.value = '编辑甜点'
  form.value = { ...row }
  dialogVisible.value = true
}

const beforeUpload = (file: File) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.error('只能上传 JPG/PNG 格式图片')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

const handleUploadSuccess = (res: any) => {
  form.value.photoUrl = res.data || res
  ElMessage.success('图片上传成功')
}

const save = async () => {
  try {
    await formRef.value?.validate()
    if (isEdit.value) {
      await dessertApi.update(form.value)
      ElMessage.success('编辑成功')
    } else {
      await dessertApi.add(form.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await fetchList()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定删除该甜点吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await dessertApi.delete(id)
      ElMessage.success('删除成功')
      await fetchList()
    } catch {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchCategories()
  fetchList()
})
</script>

<style scoped>
.container { max-width: 1200px; margin: 0 auto; padding: 0 20px; }
h2 { margin-bottom: 20px; }
.toolbar { margin-bottom: 20px; display: flex; align-items: center; }
.pagination { margin-top: 20px; display: flex; justify-content: center; }
</style>
