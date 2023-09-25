# EPortal
帮助您自动登录河海大学校园网
***
### 特性
- 成功登录校园网后保存账号密码

- 启动软件时自动尝试登录（如果成功保存了账号密码）
>基于此特性，您可以设置软件在开机时自启动，以实现完全自动登录的功能
### 使用说明
- 第一行的`Current Status`会显示您当前的在线状态，每2.5s刷新一次
- 输入账号密码后，在下方的选择框中，选择`WAN`即校园外网，`LAN`即校园内网
- 点击`Login`即可登录，点击`Logout`即可登出
- 由于状态信息更新有延迟，若您已成功登录但按钮仍显示`Login`，稍等即可
### 已知问题
读取服务器返回的信息时编码错误
### 下载
[GitHub Release](https://github.com/RedDragon0293/EPortal/releases)

- 若您的电脑中未安装Java，请下载`EPortal-1.0-jreBundled.zip`，解压后运行`EPortal.exe`
- 若已安装Java，请下载`EPortal-1.0-noJre.zip`，解压后运行`EPortal-1.0-SNAPSHOT-runnable.jar`
  **请注意**：软件仅在Java17环境下经过测试，若您无法判断Java版本或下载`EPortal-1.0-noJre.zip`
  之后无法正常运行，请下载`EPortal-1.0-jreBundled.zip`