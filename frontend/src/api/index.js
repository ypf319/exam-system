import request from './request'

export const authApi = {
  login: data => request.post('/auth/login', data),
  logout: () => request.post('/auth/logout'),
  me: () => request.get('/auth/me')
}

export const userApi = {
  page: params => request.get('/users', { params }),
  add: data => request.post('/users', data),
  update: (id, data) => request.put(`/users/${id}`, data),
  updateStatus: (id, status) => request.put(`/users/${id}/status`, null, { params: { status } }),
  resetPassword: id => request.put(`/users/${id}/password/reset`),
  remove: id => request.delete(`/users/${id}`)
}

export const courseApi = {
  list: () => request.get('/courses'),
  page: params => request.get('/courses/page', { params }),
  add: data => request.post('/courses', data),
  update: (id, data) => request.put(`/courses/${id}`, data),
  remove: id => request.delete(`/courses/${id}`)
}

export const questionApi = {
  page: params => request.get('/questions', { params }),
  detail: id => request.get(`/questions/${id}`),
  add: data => request.post('/questions', data),
  update: (id, data) => request.put(`/questions/${id}`, data),
  remove: id => request.delete(`/questions/${id}`),
  importExcel: (courseId, file) => {
    const form = new FormData()
    form.append('file', file)
    form.append('courseId', courseId)
    return request.post('/questions/import', form)
  }
}

export const paperApi = {
  page: params => request.get('/papers', { params }),
  detail: id => request.get(`/papers/${id}`),
  createManual: data => request.post('/papers/manual', data),
  createAuto: data => request.post('/papers/auto', data),
  updateStatus: (id, status) => request.put(`/papers/${id}/status`, null, { params: { status } }),
  remove: id => request.delete(`/papers/${id}`)
}

export const examApi = {
  available: () => request.get('/exam/available'),
  start: paperId => request.post(`/exam/${paperId}/start`),
  submit: (paperId, data) => request.post(`/exam/${paperId}/submit`, data)
}

export const recordApi = {
  my: params => request.get('/records/my', { params }),
  page: params => request.get('/records', { params }),
  statistics: params => request.get('/records/statistics', { params }),
  detail: id => request.get(`/records/${id}`),
  questions: id => request.get(`/records/${id}/questions`)
}
