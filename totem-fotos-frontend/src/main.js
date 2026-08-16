import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'

import BoasVindas from './views/BoasVindas.vue'
import Home from './views/Home.vue'
import Capturar from './views/Capturar.vue'
import QrCelular from './views/QrCelular.vue'
import Revisar from './views/Revisar.vue'
import RevisarImpressao from './views/RevisarImpressao.vue'
import Pagamento from './views/Pagamento.vue'
import Impressao from './views/Impressao.vue'
import UploadCelular from './views/UploadCelular.vue'

const routes = [
  { path: '/', component: BoasVindas },
  { path: '/home', component: Home },
  { path: '/capturar', component: Capturar },
  { path: '/qr-celular', component: QrCelular },
  { path: '/revisar', component: Revisar },
  { path: '/revisar-impressao', component: RevisarImpressao },
  { path: '/pagamento', component: Pagamento },
  { path: '/imprimindo', component: Impressao },
  { path: '/upload-celular', component: UploadCelular }
]

const router = createRouter({ history: createWebHistory(), routes })

createApp(App).use(router).mount('#app')
