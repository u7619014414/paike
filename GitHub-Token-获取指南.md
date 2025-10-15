# GitHub Personal Access Token 获取指南

## 为什么需要Token？

由于您使用Google账号（u7619014414@gmail.com）登录GitHub，在使用Git命令行推送代码时，不能直接使用Google密码，而需要使用GitHub的Personal Access Token作为密码。

## 📝 获取Token的步骤

### 第1步：登录GitHub并访问Token设置页面

1. 打开浏览器，访问GitHub：https://github.com
2. 确保您已经用Google账号登录（右上角应该显示您的头像）
3. 直接访问这个链接：**https://github.com/settings/tokens**

   或者手动导航：
   - 点击右上角头像
   - 选择 **Settings**（设置）
   - 在左侧菜单向下滚动，找到 **Developer settings**（开发者设置）
   - 点击 **Personal access tokens** → **Tokens (classic)**

### 第2步：生成新Token

1. 点击右上角的 **Generate new token** 按钮
2. 在下拉菜单中选择 **Generate new token (classic)**
3. 可能需要再次输入GitHub密码或进行身份验证

### 第3步：配置Token权限

在Token创建页面，填写以下信息：

#### Note（备注名称）
```
paike-project-push
```
*说明：给这个token起个名字，方便以后识别用途*

#### Expiration（有效期）
建议选择：
- **90 days**（90天）- 推荐
- 或 **No expiration**（永不过期）- 如果您想长期使用

#### Select scopes（选择权限）
**必须勾选的权限**：
- ✅ **repo** - 完整的仓库访问权限
  - 这会自动勾选以下子项：
    - ✅ repo:status
    - ✅ repo_deployment
    - ✅ public_repo
    - ✅ repo:invite
    - ✅ security_events

**可选权限**（根据需要勾选）：
- ✅ **workflow** - 如果您的项目使用GitHub Actions
- ✅ **read:org** - 如果仓库属于某个组织

**不需要勾选**：
- ❌ admin:repo_hook
- ❌ delete_repo
- ❌ admin:org（除非您需要管理组织）

### 第4步：生成并保存Token

1. 向下滚动到页面底部
2. 点击绿色按钮 **Generate token**
3. **重要**：Token会显示一次，必须立即复制！

您会看到类似这样的Token：
```
ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

**立即复制这个Token！** 关闭页面后将无法再次查看。

### 第5步：保存Token（重要！）

建议您将Token保存在安全的地方，例如：

1. **记事本文件**（保存到安全位置）：
   ```
   GitHub Token for paike project
   Created: 2025-10-15
   Token: ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
   ```

2. **密码管理器**（如LastPass、1Password等）

3. **临时文本文件**：
   ```
   D:\1vueproj\paike\github-token.txt
   ```
   **注意**：此文件已添加到.gitignore，不会被提交

## 🚀 使用Token推送代码

获取Token后，在命令行执行：

### 方法A：使用Git凭据管理器（推荐）

```bash
cd D:\1vueproj\paike
git push origin develop
```

Windows会弹出凭据输入窗口，输入：
- **用户名**: `u7619014414-lang`
- **密码**: `ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`（您的Token）

### 方法B：在命令中直接使用Token

```bash
cd D:\1vueproj\paike
git push https://u7619014414-lang:ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx@github.com/u7619014414-lang/paike.git develop
```

将 `ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx` 替换为您的实际Token。

### 方法C：配置Git存储凭据

如果您不想每次都输入Token，可以让Git记住：

```bash
# 配置Git存储凭据
git config --global credential.helper store

# 然后推送（只需输入一次）
cd D:\1vueproj\paike
git push origin develop
```

输入用户名和Token后，Git会将其保存到：
```
C:\Users\<您的用户名>\.git-credentials
```

## ✅ 验证推送成功

推送成功后，您会看到类似输出：

```
Enumerating objects: 547, done.
Counting objects: 100% (547/547), done.
Delta compression using up to 8 threads
Compressing objects: 100% (341/341), done.
Writing objects: 100% (456/456), 123.45 KiB | 6.17 MiB/s, done.
Total 456 (delta 234), reused 0 (delta 0), pack-reused 0
remote: Resolving deltas: 100% (234/234), completed with 45 local objects.
To https://github.com/u7619014414-lang/paike.git
   829e91d0..737eafd8  develop -> develop
```

然后访问：**https://github.com/u7619014414-lang/paike/tree/develop**
查看您的代码！

## 🔒 安全提示

1. **不要分享Token** - Token就像密码一样重要
2. **不要将Token提交到代码仓库** - 已配置.gitignore避免此问题
3. **定期更换Token** - 建议每90天更换一次
4. **Token泄露后立即删除** - 访问 https://github.com/settings/tokens 删除旧Token

## ❓ 常见问题

### Q1: Token输入后还是提示403错误？
**A**: 检查以下几点：
- Token是否复制完整（没有多余空格）
- 是否勾选了 `repo` 权限
- Token是否已过期
- 用户名是否正确（`u7619014414-lang`）

### Q2: 忘记保存Token怎么办？
**A**: Token只显示一次，忘记后只能：
1. 删除旧Token
2. 重新生成新Token
3. 使用新Token推送

### Q3: 如何查看或删除Token？
**A**: 访问 https://github.com/settings/tokens
- 可以看到所有Token的列表
- 点击Token名称查看详情（但看不到实际Token值）
- 点击 **Delete** 删除不需要的Token

### Q4: Token和密码有什么区别？
**A**:
- **密码**：用于登录GitHub网站
- **Token**：用于Git命令行操作（push、pull等）
- 使用Google登录的账号没有GitHub密码，只能用Token

## 📞 需要帮助？

如果在获取或使用Token过程中遇到问题：
1. 检查GitHub邮箱（u7619014414@gmail.com）是否有GitHub的通知
2. 确认您的GitHub账号是 `u7619014414-lang`
3. 查看本文件同目录下的 `快速开始.md` 了解项目使用方法

---

**创建日期**: 2025-10-15
**适用于**: paike项目代码推送
**GitHub账号**: u7619014414-lang (u7619014414@gmail.com)
