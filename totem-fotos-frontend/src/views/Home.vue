<template>
  <div class="tela home">
    <div class="cabecalho">
      <p class="titulo">Escolha sua foto</p>
      <p class="subtitulo">Selecione o formato que deseja imprimir</p>
    </div>

    <div class="produtos">
      <button
        v-for="produto in produtos"
        :key="produto.id"
        type="button"
        class="produto"
        :class="{ selecionado: sessao.produto?.id === produto.id }"
        @click="sessao.produto = produto"
      >
        <span>
          <strong>{{ produto.nome }}</strong>
          <small>{{ produto.detalhe }}</small>
        </span>
        <strong class="preco">{{ formatarValor(produto.valor) }}</strong>
      </button>
    </div>

    <div class="acoes">
      <p class="instrucao">
        {{ sessao.produto ? 'Agora escolha como enviar a foto' : 'Selecione uma opção para continuar' }}
      </p>
      <button class="botao botao-primario" :disabled="!sessao.produto" @click="tirarFoto">
        Tirar foto agora
      </button>
      <button class="botao botao-secundario" :disabled="!sessao.produto" @click="enviarDoCelular">
        Enviar do celular
      </button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import api from '../services/api'
import { sessao } from '../services/sessaoState'

const router = useRouter()

const produtos = [
  { id: 'POLAROID', nome: 'Polaroid', detalhe: '1 foto', valor: 4.50 },
  { id: 'NORMAL_10X15', nome: 'Normal 10x15', detalhe: '1 foto', valor: 5.50 },
  { id: 'SEIS_FOTOS_3X4', nome: 'Fotos 3x4', detalhe: 'Conjunto com 6 fotos', valor: 19.90 }
]

const formatarValor = (valor) => valor.toLocaleString('pt-BR', {
  style: 'currency',
  currency: 'BRL'
})

async function garantirSessao() {
  if (!sessao.id) {
    const { data } = await api.post('/sessoes', { produto: sessao.produto.id })
    sessao.id = data.id
  }
}

async function tirarFoto() {
  await garantirSessao()
  router.push('/capturar')
}

async function enviarDoCelular() {
  await garantirSessao()
  router.push('/qr-celular')
}
</script>

<style scoped>
.home {
  align-items: stretch;
}

.cabecalho {
  margin-top: 22px;
  text-align: center;
}

.titulo {
  font-size: 26px;
  font-weight: 700;
}

.subtitulo,
.instrucao,
.produto small {
  color: #6b6b66;
}

.subtitulo {
  margin-top: 6px;
  font-size: 14px;
}

.produtos {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.produto {
  width: 100%;
  min-height: 92px;
  padding: 16px 18px;
  border: 2px solid #e6e4dc;
  border-radius: 16px;
  background: #fff;
  color: #1c1c1a;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  text-align: left;
  cursor: pointer;
}

.produto.selecionado {
  border-color: #1c1c1a;
  background: #f5f4ef;
  box-shadow: inset 0 0 0 1px #1c1c1a;
}

.produto strong,
.produto small {
  display: block;
}

.produto strong {
  font-size: 17px;
}

.produto small {
  margin-top: 5px;
  font-size: 13px;
}

.produto .preco {
  flex: none;
  font-size: 18px;
}

.acoes {
  margin-top: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.instrucao {
  min-height: 20px;
  text-align: center;
  font-size: 13px;
}

.botao:disabled {
  opacity: .42;
  cursor: not-allowed;
}
</style>
