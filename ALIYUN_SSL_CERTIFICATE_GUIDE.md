# 阿里云SSL证书申请和使用指南

## 目录

1. [证书类型选择](#证书类型选择)
2. [免费证书申请流程](#免费证书申请流程)
3. [付费证书申请流程](#付费证书申请流程)
4. [证书下载和配置](#证书下载和配置)
5. [在Nginx中使用证书](#在nginx中使用证书)
6. [证书续期和更新](#证书续期和更新)

---

## 证书类型选择

### 免费证书（DV证书）
- **适用场景**：个人网站、测试环境、小型项目
- **有效期**：1年
- **价格**：免费
- **验证方式**：域名验证（DNS或文件验证）
- **限制**：每个账号每年最多20个免费证书

### 付费证书（OV/EV证书）
- **适用场景**：企业网站、电商平台、需要更高信任度的网站
- **有效期**：1-3年
- **价格**：根据类型和年限不同
- **验证方式**：企业信息验证
- **优势**：更高的信任度，显示企业名称

**推荐**：对于大多数项目，免费DV证书已足够使用。

---

## 免费证书申请流程

### 步骤1：登录阿里云控制台

1. 访问 [阿里云官网](https://www.aliyun.com/)
2. 登录你的阿里云账号
3. 进入 **产品与服务** → **安全** → **SSL证书（应用安全）**

或者直接访问：https://yundun.console.aliyun.com/?p=cas

### 步骤2：购买免费证书

1. 点击 **SSL证书** → **免费证书**
2. 点击 **立即购买** 或 **创建证书**
3. 选择 **免费型DV SSL**（如果显示）
4. 点击 **立即购买**（价格为0元）
5. 确认订单并支付（免费订单）

### 步骤3：申请证书

1. 在证书列表中，找到刚购买的免费证书
2. 点击 **证书申请** 或 **立即申请**
3. 填写证书信息：

   **必填信息**：
   - **证书绑定域名**：
     - 单域名：`your-domain.com`
     - 通配符：`*.your-domain.com`（免费证书不支持通配符）
     - 多域名：需要购买付费证书
   
   - **域名验证方式**：
     - **DNS验证**（推荐）：在域名DNS解析中添加TXT记录
     - **文件验证**：在网站根目录上传验证文件
   
   - **CSR生成方式**：
     - **系统生成**（推荐）：阿里云自动生成
     - **手动填写**：自己生成CSR

4. 点击 **提交审核**

### 步骤4：域名验证

#### 方式一：DNS验证（推荐）

1. 提交申请后，系统会显示需要添加的DNS记录
2. 登录你的域名DNS管理控制台（如阿里云DNS、Cloudflare等）
3. 添加TXT记录：
   ```
   记录类型：TXT
   主机记录：_dnsauth（或系统提示的记录名）
   记录值：系统提供的验证值（一串长字符串）
   TTL：600（或默认）
   ```
4. 等待DNS解析生效（通常几分钟到几小时）
5. 在阿里云证书控制台点击 **验证** 或等待自动验证

#### 方式二：文件验证

1. 系统会提供验证文件路径和内容
2. 在你的网站根目录创建验证文件
3. 确保可以通过 `http://your-domain.com/.well-known/pki-validation/验证文件名` 访问
4. 点击 **验证**

### 步骤5：等待审核

- DNS验证：通常几分钟到几小时内完成
- 文件验证：通常几分钟内完成
- 审核通过后，证书状态变为 **已签发**

---

## 付费证书申请流程

### 步骤1：选择证书类型

1. 进入SSL证书控制台
2. 选择 **付费证书** → **购买证书**
3. 选择证书类型：
   - **DV证书**：域名验证，价格较低
   - **OV证书**：组织验证，显示企业名称
   - **EV证书**：扩展验证，最高信任度（地址栏显示绿色）

### 步骤2：购买证书

1. 选择证书品牌（DigiCert、GlobalSign、Symantec等）
2. 选择证书年限（1年、2年、3年）
3. 选择域名类型（单域名、多域名、通配符）
4. 确认订单并支付

### 步骤3：申请证书

1. 在证书列表中找到购买的证书
2. 点击 **证书申请**
3. 填写详细信息：
   - **域名信息**
   - **公司信息**（OV/EV证书需要）
   - **联系人信息**
   - **CSR信息**

### 步骤4：提交审核

- **DV证书**：域名验证（同免费证书）
- **OV证书**：需要提交企业营业执照等资料
- **EV证书**：需要更严格的企业验证流程

审核时间：
- DV证书：几分钟到几小时
- OV证书：1-3个工作日
- EV证书：3-7个工作日

---

## 证书下载和配置

### 步骤1：下载证书

1. 证书审核通过后，在证书列表中点击 **下载**
2. 选择服务器类型：**Nginx**
3. 下载证书文件包（通常包含以下文件）：
   - `your-domain.com.pem`（证书文件）
   - `your-domain.com.key`（私钥文件）
   - `chain.pem`（证书链文件，可选）

### 步骤2：上传证书到服务器

```bash
# 创建SSL证书目录
sudo mkdir -p /etc/nginx/ssl
sudo chmod 700 /etc/nginx/ssl

# 上传证书文件（使用scp）
scp your-domain.com.pem user@your-server:/tmp/
scp your-domain.com.key user@your-server:/tmp/

# 在服务器上移动文件
sudo mv /tmp/your-domain.com.pem /etc/nginx/ssl/
sudo mv /tmp/your-domain.com.key /etc/nginx/ssl/

# 设置权限（重要！）
sudo chmod 644 /etc/nginx/ssl/your-domain.com.pem
sudo chmod 600 /etc/nginx/ssl/your-domain.com.key
sudo chown root:root /etc/nginx/ssl/*
```

### 步骤3：合并证书链（如果需要）

如果下载的证书包中有单独的证书链文件，需要合并：

```bash
# 合并证书和证书链
sudo cat your-domain.com.pem chain.pem > /etc/nginx/ssl/your-domain.com-fullchain.pem
```

---

## 在Nginx中使用证书

### 配置Nginx使用阿里云证书

编辑Nginx配置文件：

```bash
sudo nano /etc/nginx/sites-available/dataelf
```

配置内容：

```nginx
server {
    listen 443 ssl http2;
    server_name your-domain.com www.your-domain.com;
    
    # 阿里云SSL证书配置
    ssl_certificate /etc/nginx/ssl/your-domain.com.pem;
    ssl_certificate_key /etc/nginx/ssl/your-domain.com.key;
    
    # 如果有证书链文件，使用完整链
    # ssl_certificate /etc/nginx/ssl/your-domain.com-fullchain.pem;
    
    # SSL优化配置
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers 'ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384';
    ssl_prefer_server_ciphers on;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;
    
    # 其他配置...
    root /opt/dataelf/frontend/dist;
    index index.html;
    
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

# HTTP重定向到HTTPS
server {
    listen 80;
    server_name your-domain.com www.your-domain.com;
    return 301 https://$server_name$request_uri;
}
```

### 测试和重启Nginx

```bash
# 测试配置
sudo nginx -t

# 如果测试通过，重启Nginx
sudo systemctl reload nginx

# 或者重启
sudo systemctl restart nginx
```

### 验证HTTPS

```bash
# 测试HTTPS连接
curl -I https://your-domain.com

# 检查证书信息
openssl s_client -connect your-domain.com:443 -servername your-domain.com
```

---

## 证书续期和更新

### 免费证书续期

免费证书有效期为1年，需要每年续期：

1. **提前续期**：证书到期前30天可以申请续期
2. **续期步骤**：
   - 登录阿里云SSL证书控制台
   - 找到即将到期的证书
   - 点击 **续费** 或 **重新申请**
   - 按照新证书申请流程操作
3. **更新证书**：
   - 下载新证书
   - 上传到服务器替换旧证书
   - 重新加载Nginx

### 自动化续期脚本

创建续期脚本：

```bash
cat > /opt/dataelf/scripts/renew-ssl.sh << 'EOF'
#!/bin/bash
# SSL证书续期脚本
# 注意：此脚本需要手动下载新证书后执行

CERT_DIR="/etc/nginx/ssl"
BACKUP_DIR="/opt/dataelf/ssl-backup"
DATE=$(date +%Y%m%d)

echo "开始更新SSL证书..."

# 创建备份目录
mkdir -p $BACKUP_DIR

# 备份旧证书
if [ -f "$CERT_DIR/your-domain.com.pem" ]; then
    echo "备份旧证书..."
    cp $CERT_DIR/your-domain.com.pem $BACKUP_DIR/your-domain.com.pem.$DATE
    cp $CERT_DIR/your-domain.com.key $BACKUP_DIR/your-domain.com.key.$DATE
fi

# 新证书文件应该已经上传到 /tmp/
# 移动新证书
if [ -f "/tmp/your-domain.com.pem" ]; then
    echo "安装新证书..."
    sudo mv /tmp/your-domain.com.pem $CERT_DIR/
    sudo mv /tmp/your-domain.com.key $CERT_DIR/
    sudo chmod 644 $CERT_DIR/your-domain.com.pem
    sudo chmod 600 $CERT_DIR/your-domain.com.key
    sudo chown root:root $CERT_DIR/your-domain.com.*
    
    # 测试Nginx配置
    if sudo nginx -t; then
        echo "重新加载Nginx..."
        sudo systemctl reload nginx
        echo "证书更新成功！"
    else
        echo "Nginx配置错误，恢复旧证书..."
        sudo cp $BACKUP_DIR/your-domain.com.pem.$DATE $CERT_DIR/your-domain.com.pem
        sudo cp $BACKUP_DIR/your-domain.com.key.$DATE $CERT_DIR/your-domain.com.key
        exit 1
    fi
else
    echo "错误：未找到新证书文件 /tmp/your-domain.com.pem"
    exit 1
fi
EOF

chmod +x /opt/dataelf/scripts/renew-ssl.sh
```

### 证书到期提醒

在阿里云控制台设置：
1. 进入 **SSL证书** → **证书到期提醒**
2. 设置提醒时间（建议提前30天）
3. 配置通知方式（邮件、短信）

---

## 常见问题

### Q1: 免费证书支持通配符域名吗？
**A**: 不支持。免费DV证书只支持单域名。如果需要通配符证书（`*.your-domain.com`），需要购买付费证书。

### Q2: 证书申请后多久能生效？
**A**: 
- DNS验证：通常几分钟到几小时内
- 文件验证：通常几分钟内
- OV/EV证书：1-7个工作日

### Q3: 证书可以用于多个域名吗？
**A**: 免费证书只能用于单个域名。如果需要多个域名，需要：
- 为每个域名申请单独的证书
- 或购买多域名证书（付费）

### Q4: 证书文件权限设置错误会怎样？
**A**: Nginx可能无法读取证书，导致HTTPS无法工作。确保：
- 证书文件（.pem）：644权限
- 私钥文件（.key）：600权限
- 所有者：root:root

### Q5: 如何检查证书是否安装成功？
**A**: 
```bash
# 方法1：浏览器访问
访问 https://your-domain.com，查看地址栏锁图标

# 方法2：命令行检查
openssl s_client -connect your-domain.com:443 -servername your-domain.com

# 方法3：在线工具
访问 https://www.ssllabs.com/ssltest/ 输入域名检测
```

### Q6: 证书续期后需要重启服务器吗？
**A**: 不需要重启服务器，只需要重新加载Nginx：
```bash
sudo systemctl reload nginx
```

### Q7: 阿里云证书和Let's Encrypt证书有什么区别？
**A**: 
- **阿里云证书**：需要手动申请和续期，有中文支持，适合国内用户
- **Let's Encrypt**：可以自动化申请和续期，完全免费，但需要安装Certbot工具

---

## 推荐方案对比

| 方案 | 优点 | 缺点 | 适用场景 |
|------|------|------|----------|
| 阿里云免费证书 | 中文支持，操作简单 | 需要手动续期 | 国内服务器，不熟悉命令行 |
| Let's Encrypt | 完全免费，自动续期 | 需要安装工具，英文文档 | 熟悉Linux，需要自动化 |

**建议**：
- 如果服务器在国内，推荐使用阿里云免费证书
- 如果服务器在国外，或需要自动化管理，推荐使用Let's Encrypt

---

## 相关链接

- [阿里云SSL证书控制台](https://yundun.console.aliyun.com/?p=cas)
- [SSL证书文档](https://help.aliyun.com/product/28572.html)
- [Nginx SSL配置文档](http://nginx.org/en/docs/http/configuring_https_servers.html)

---

**最后更新**：2024年

如有问题，请参考阿里云官方文档或联系阿里云技术支持。
