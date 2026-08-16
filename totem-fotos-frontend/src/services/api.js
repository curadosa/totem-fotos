import axios from 'axios'

// Ao acessar pela rede, localhost apontaria para o celular. Por padrao usamos
// o mesmo host que entregou o frontend e mantemos VITE_API_URL como override.
const hostBackend = window.location.hostname

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || `http://${hostBackend}:8080/api`
})

export default api
