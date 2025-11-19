<template>
  <nav class="nav">
    <a href="/" class="logo">拼车系统</a>
    <div class="menu">
      <router-link to="/">首页</router-link>
      <router-link to="/traffic">实时路况</router-link>
      <router-link to="/carpool">同城拼车</router-link>
      <router-link to="/user">用户中心</router-link>
    </div>
  </nav>
</template>

<style>
.nav {
  position: fixed;
  top: 0; left: 0; right: 0;
  background: #1e90ff;
  color: white;
  height: 50px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  justify-content: space-between;
}
.nav a {
  color: white;
  text-decoration: none;
  margin-left: 16px;
}
.logo {
  font-weight: bold;
}
</style>
