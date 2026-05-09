<template>
  <el-dialog :model-value="visible" :title="isEdit ? '编辑图书' : '新增图书'" width="720px" :close-on-click-modal="false" @close="handleClose">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" size="default">
      <div class="cover-upload-section">
        <label class="section-label">封面图片</label>
        <div class="upload-area">
          <div class="cover-preview" v-if="form.coverImage || previewUrl">
            <img :src="previewUrl || form.coverImage" alt="封面预览" />
            <div class="preview-actions">
              <el-button type="danger" size="small" circle @click="removeCover">
                <el-icon><Delete /></el-icon>
              </el-button>
              <el-button type="primary" size="small" circle @click="triggerUpload">
                <el-icon><Upload /></el-icon>
              </el-button>
            </div>
          </div>
          <div class="upload-placeholder" v-else @click="triggerUpload">
            <el-icon :size="36"><Picture /></el-icon>
            <p>点击上传封面</p>
            <span>支持 JPG、PNG、GIF，最大 5MB</span>
          </div>
          <input ref="fileInputRef" type="file" accept=".jpg,.jpeg,.png,.gif,.webp" style="display: none" @change="handleFileChange" />
        </div>
      </div>

      <el-row :gutter="20">
        <el-col :span="16">
          <el-form-item label="ISBN" prop="isbn"><el-input v-model="form.isbn" placeholder="请输入ISBN" /></el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="分类" prop="categoryId">
            <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 100%">
              <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="书名" prop="title"><el-input v-model="form.title" placeholder="请输入书名" /></el-form-item>
      <el-form-item label="作者" prop="author"><el-input v-model="form.author" placeholder="请输入作者" /></el-form-item>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="出版社"><el-input v-model="form.publisher" placeholder="请输入出版社" /></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="出版日期"><el-date-picker v-model="form.publishDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="价格(元)"><el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="页数"><el-input-number v-model="form.pages" :min="0" style="width: 100%" /></el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="总库存" prop="stockTotal"><el-input-number v-model="form.stockTotal" :min="1" style="width: 100%" /></el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="馆藏位置"><el-input v-model="form.location" placeholder="例如：A区-3层-01架" /></el-form-item>
      <el-form-item label="简介"><el-input v-model="form.description" type="textarea" :rows="3" placeholder="可选填图书简介" /></el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Delete, Upload, Picture } from '@element-plus/icons-vue'
import type { FormInstance, FormRules, UploadFile } from 'element-plus'
import { createBook, updateBook, getBookById, uploadBookCover } from '@/api/book'
import { getCategoryList } from '@/api/category'

interface Props { visible: boolean; bookId: number | null }
const props = defineProps<Props>()
const emit = defineEmits(['update:visible', 'success'])

const formRef = ref<FormInstance>()
const fileInputRef = ref<HTMLInputElement>()
const loading = ref(false)
const uploadingCover = ref(false)
const pendingFile = ref<File | null>(null)
const previewUrl = ref('')
const isEdit = computed(() => !!props.bookId)
const categories = ref<any[]>([])

const form = reactive({
  isbn: '', title: '', author: '', publisher: '', publishDate: '',
  categoryId: undefined as number | undefined,
  price: undefined as number | undefined,
  pages: undefined as number | undefined,
  stockTotal: 5, location: '', description: '',
  coverImage: '' as string | undefined,
})

const rules: FormRules = {
  isbn: [{ required: true, message: '请输入ISBN', trigger: 'blur' }],
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  stockTotal: [{ required: true, message: '请输入总库存', trigger: 'blur' }],
}

watch(() => props.visible, async (val) => {
  if (val) {
    try {
      categories.value = await getCategoryList() || []
    } catch {}
    if (props.bookId) await loadBookDetail()
  } else resetForm()
})

async function loadBookDetail() {
  try {
    const book = await getBookById(props.bookId!)
    Object.assign(form, {
      isbn: book.isbn || '',
      title: book.title || '',
      author: book.author || '',
      publisher: book.publisher || '',
      publishDate: book.publishDate || '',
      categoryId: book.categoryId,
      price: book.price,
      pages: book.pages,
      stockTotal: book.stockTotal || 5,
      location: book.location || '',
      description: book.description || '',
      coverImage: book.coverImage || '',
    })
    previewUrl.value = ''
    pendingFile.value = null
  } catch {}
}

function resetForm() {
  if (formRef.value) formRef.value.resetFields()
  Object.assign(form, { isbn: '', title: '', author: '', publisher: '', publishDate: '', categoryId: undefined, price: undefined, pages: undefined, stockTotal: 5, location: '', description: '', coverImage: '' })
  previewUrl.value = ''
  pendingFile.value = null
}

function triggerUpload() { fileInputRef.value?.click() }

async function handleFileChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  const validTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!validTypes.includes(file.type)) { ElMessage.error('仅支持 JPG、PNG、GIF、WEBP 格式'); return }
  if (file.size > 5 * 1024 * 1024) { ElMessage.error('图片大小不能超过5MB'); return }

  previewUrl.value = URL.createObjectURL(file)
  pendingFile.value = file
  input.value = ''
}

function removeCover() {
  form.coverImage = ''
  previewUrl.value = ''
  pendingFile.value = null
}

function handleClose() { emit('update:visible', false) }

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      let bookId = props.bookId
      if (isEdit.value) {
        await updateBook({ id: props.bookId!, ...form })
      } else {
        const res = await createBook(form)
        bookId = res?.id
      }

      if (pendingFile.value && bookId) {
        uploadingCover.value = true
        try {
          const coverPath = await uploadBookCover(bookId, pendingFile.value)
          form.coverImage = typeof coverPath === 'string' ? coverPath : (coverPath as any)?.data || ''
        } catch (e: any) {
          ElMessage.warning('图书保存成功，但封面上传失败')
        } finally { uploadingCover.value = false }
      }

      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      handleClose()
      emit('success')
    } finally { loading.value = false }
  })
}
</script>

<style lang="scss" scoped>
.cover-upload-section {
  margin-bottom: 20px;
  .section-label {
    display: block;
    font-size: 14px;
    font-weight: 500;
    color: var(--text-regular);
    margin-bottom: 10px;
  }
}

.upload-area {
  .cover-preview {
    width: 160px;
    height: 210px;
    border-radius: 10px;
    overflow: hidden;
    position: relative;
    border: 2px solid var(--border-color);
    img { width: 100%; height: 100%; object-fit: cover; }
    .preview-actions {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      padding: 8px;
      background: linear-gradient(transparent, rgba(0,0,0,0.6));
      display: flex;
      justify-content: center;
      gap: 6px;
      opacity: 0;
      transition: opacity 0.25s;
    }
    &:hover .preview-actions { opacity: 1; }
  }

  .upload-placeholder {
    width: 160px;
    height: 210px;
    border-radius: 10px;
    border: 2px dashed var(--border-color);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.3s;
    color: var(--text-secondary);

    &:hover {
      border-color: var(--primary-color);
      color: var(--primary-color);
      background: var(--primary-bg);
    }

    p { margin: 8px 0 4px; font-size: 13px; font-weight: 500; }
    span { font-size: 11px; color: var(--text-placeholder); }
  }
}
</style>
