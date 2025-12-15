<template>
  <div id="app">
    <NavBar />
    <router-view />
  </div>
</template>

<script>
import NavBar from './components/NavBar.vue'

export default {
  components: { NavBar }
}
</script>

<style>
body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell', sans-serif;
  background-color: #f5f7fa;
}

#app {
  padding-top: 0;
}

* {
  box-sizing: border-box;
}

html, body {
  height: 100%;
  overflow-x: hidden;
}
</style>
