# EPortal

帮助您自动登录河海大学校园网
***

### 特性

- 成功登录校园网后保存账号密码
- 启动软件时自动尝试登录（如果成功保存了账号密码）

> 基于此特性，您可以设置软件在开机时自启动，以实现完全自动登录的功能

### 下载

- [GitHub Release](https://github.com/RedDragon0293/EPortal/releases)
- [Github Actions](https://github.com/RedDragon0293/EPortal/actions)

在1.3.0版本后, 新的版本将不会在 [Release]() 中发布, 而是在 [Actions]() 中自动构建.  
不建议再使用1.3.0及之前的任何版本. **当且仅当您想使用内置JVM的版本时**, 可以下载后缀为`JreBundled`的版本,
解压后运行`EPortal.exe`.

### 使用说明

- `Status`栏会显示您的在线状态、当前登录的运营商 (如果在线的话)
- `User`栏会显示当前登录的用户名及学号
- `Time Remaining`会显示当前套餐剩余时间 (仅对校园网有效)
- 输入账号密码, 选择运营商后即可登录
- 如果此设备校园网已经在线, 无论输入框中是否有内容, 点击`Logout`都会立即登出在线账号

> ***注意:***
> 1. 当在其他地方 (如浏览器) 登录校园网后, 当前登录的用户可能与输入框中的用户不同.
> 2. 软件仅在 Java17 下测试编译通过且正常运行. 若您下载后无法正常运行, 请考虑使用 Java17或更高版本.

### 已知问题

~~读取服务器返回的信息时编码错误~~