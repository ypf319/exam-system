const USER_KEY = 'exam_user'

export function getUser() {
  try {
    const value = localStorage.getItem(USER_KEY)
    return value ? JSON.parse(value) : null
  } catch (e) {
    return null
  }
}

export function setUser(user) {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function removeUser() {
  localStorage.removeItem(USER_KEY)
}

export function homePath(role) {
  if (role === 1) return '/admin/users'
  if (role === 2) return '/teacher/questions'
  return '/student/papers'
}
