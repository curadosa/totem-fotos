<template>
  <router-view />
</template>

<style>
* { box-sizing: border-box; margin: 0; padding: 0; }
body {
  font-family: system-ui, sans-serif;
  background: #1c1c1a;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.tela {
  width: 480px;
  height: 800px;
  background: #fff;
  border-radius: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32px 24px;
  gap: 20px;
}
.botao {
  width: 100%;
  padding: 20px;
  border-radius: 14px;
  border: none;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
}
.botao-primario { background: #1c1c1a; color: #fff; }
.botao-secundario { background: #f0efe9; color: #1c1c1a; }
</style>
