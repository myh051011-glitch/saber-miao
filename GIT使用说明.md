# 本地版本管理

仓库位置：桌面／随日项目／源码／SuiRi。
首次存档：saber喵 1.1.5，分支 main，标签 v1.1.5。
仓库只在本地，未配置 GitHub 远程地址。

在源码目录打开终端：

```powershell
git status
git diff
git log --oneline --decorate
```

每次改动验证后存档：

```powershell
git add .
git commit -m "描述本次修改"
```

首次提交使用本仓库专用作者 Local Developer <local@localhost>。
需要时用 git config user.name 和 git config user.email 设置后续提交作者。

APK、构建缓存、API配置、个人数据库与签名私钥不入库。
已有签名密钥仍在工作目录 work/suiri-build/personal-debug.p12，应单独妥善备份；重新生成密钥会影响覆盖升级。
此标签记录当前源码，不代表重新进行了手机验收。
