<template>
  <div class="modal-mask" v-if="state.dialogVisible">
    <div class="modal-container" @keyup.esc="handleClose" style="width: 960px">
      <div class="modal-header">
        클래스 개설하기
        <svg
          class="btn-modal-close"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
          xmlns="http://www.w3.org/2000/svg"
          @click="handleClose"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M6 18L18 6M6 6l12 12"
          ></path>
        </svg>
      </div>
      <hr class="modal-hr">
      <br>
      <Form @submit="clickConference" :validation-schema="schema" class="modal-body">
        <select v-model="state.form.conferenceCategoryId" class="">
          <option value="" selected disabled hidden>카테고리</option>
          <option v-for="(item, idx) in state.conferenceIds" :key="idx" :value="item.id"> {{ item.name }}</option>
        </select>
        <TextInput
          name="title"
          type="text"
          placeholder="클래스 제목"
        />
        <TextInput
          name="description"
          type="text"
          placeholder="클래스 설명"
        />
        <TextInput
          v-if="!state.isFreeClass"
          name="price"
          type="text"
          placeholder="클래스 가격"
        />
        <input type="checkbox" v-model="state.isFreeClass" id="isFreeClass">
        <label for="isFreeClass" style="font-size: 14px;">무료 강의를 개설하겠습니다.</label>
        <div class="modal-group">
          <label for="input-thumbnail" class="label-modal-thumbnail">
            썸네일 선택
          </label>
          <input
          type="file"
          id="input-thumbnail"
          style="display:none"
          @change="fileSelect"/>
          <button class="btn btn-submit" type="submit" style="width: 308px; margin: 10 0;">개설하기</button>
        </div>
        <p class="modal-thumbnail-selected-title" v-if="state.form.thumbnail">
          파일명: {{ state.form.thumbnail.name }}
        </p>
        <p class="modal-thumbnail-selected-title" v-else>
          썸네일을 위한 사진 파일을 선택해 주세요.
        </p>
      </Form>
    </div>
  </div>
</template>

<script>
import { reactive, ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import Swal from 'sweetalert2'
import { Form } from 'vee-validate'
import * as Yup from 'yup'
import TextInput from '@/components/TextInput'

export default {
  name: 'conference-dialog',
  props: {
    open: {
      type: Boolean,
      default: false
    },
    width: {
      type: Number,
      default: 960
    }
  },
  components: {
    TextInput,
    Form,
  },

  setup(props, { emit }) {
    const store = useStore()
    const router = useRouter()
    const conferenceForm = ref(null)

    const state = reactive({
      form: {
        conferenceCategoryId: '',
        thumbnail: null,
        align: 'left'
      },
      dialogVisible: computed(() => props.open),
      formLabelWidth: '120px',
      isSpinning: false,
      conferenceIds: computed(() => store.getters['root/getConferenceId']),
      isFreeClass: false,
    })

    let schema = Yup.object().shape({
      title: Yup
        .string()
        .required('필수 입력 항목입니다.'),
      description: Yup
        .string()
        .required('필수 입력 항목입니다.'),
      price: Yup
        .number('숫자를 입력해 주세요.')
        .typeError('숫자를 입력해 주세요')
        .moreThan(99, '100 이상의 수만 입력이 가능합니다.')
        .integer('너무 큰 수는 입력이 불가능합니다.')
    })
    const thumbnailRegExp = /.*\.(jpg|jpeg|png|gif|jfif)$/
    const maxSize = 5 * 1024 * 1024

    const thumbnailValidate = function (value) {
      if (value === null) {
        alert('첨부파일은 필수 항목입니다.')
        return
      } else {
        let thumbnailSize = value.size

        if (!thumbnailRegExp.test(value.name)) {
          alert('이미지 파일만 업로드 가능합니다.')
          return state.form.thumbnail = null
        } else if (thumbnailSize > maxSize) {
          alert('5MB 이하의 파일만 업로드 가능합니다.')
          return state.form.thumbnail = null
        } else {
          return true
        }
      }
    }

    onMounted(() => {
      store.dispatch('root/requestConferenceId')
    })

    const clickConference = function (value) {
      if (thumbnailValidate(state.form.thumbnail)) {

        store.commit('root/setSpinnerStart')
        const formData = new FormData()
        formData.append('title', value.title)
        if (state.isFreeClass) {
          formData.append('price', 0)
        }
        formData.append('price', value.price)
        formData.append('conferenceCategoryId', state.form.conferenceCategoryId)
        formData.append('description', value.description)
        formData.append('thumbnail', state.form.thumbnail)
        store.dispatch('root/requestConferenceCreate', formData)
        .then(function (res) {
            Swal.fire({
            icon: 'success',
            html: '컨퍼런스가 생성되었습니다.',
            showConfirmButton: false,
            timer:1000
          })
          setTimeout(function(){
            emit('closeConferenceDialog')
            router.push({
              name: 'conference-detail',
              params: {
                conferenceId: res.data.conferenceId
              }
            })
          }, 1000);
        })
        .catch(function (err) {
          Swal.fire({
          icon: 'error',
          html: err.response.data.message,
          showConfirmButton: false,
          timer: 1000
          })
        })
        .finally(store.commit('root/setSpinnerEnd'))
      }
    }

    const fileSelect = function (event) {
      state.form.thumbnail = event.target.files[0]
    }

    const handleClose = function () {
      emit('closeConferenceDialog')
    }

    return { conferenceForm, state, clickConference, handleClose, fileSelect, schema }
  }
}
</script>
