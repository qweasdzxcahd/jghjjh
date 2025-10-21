document.addEventListener('DOMContentLoaded', () => {
    // 初始化DOM元素引用
    const linkInput = document.getElementById('link');
    const generateBtn = document.getElementById('generateBtn');
    const resultDiv = document.getElementById('result');
    const copyBtn = document.getElementById('copyBtn');
    const resultContent = document.getElementById('resultContent');
    const totalStats = document.getElementById('totalStats');
    const todayStats = document.getElementById('todayStats');
    const ninjaStats = document.getElementById('ninjaStats');
    const krnlStats = document.getElementById('krnlStats');
    const modal = document.getElementById('modal');
    const modalClose = document.getElementById('modalClose');
    const countdown = document.getElementById('countdown');
    
    // 显示欢迎弹窗（仅首次访问）
    if (!localStorage.getItem('modalClosed')) {
        modal.style.display = 'flex';
        
        let seconds = 10;
        const timer = setInterval(() => {
            seconds--;
            countdown.textContent = `${seconds}秒后可关闭...`;
            
            if (seconds <= 0) {
                clearInterval(timer);
                modalClose.classList.add('active');
                countdown.textContent = '可关闭';
            }
        }, 1000);
    } else {
        // 非首次访问，直接显示小弹窗
        createFloatingNotification();
    }
    
    // 关闭弹窗事件
    modalClose.addEventListener('click', () => {
        if (modalClose.classList.contains('active')) {
            modal.style.display = 'none';
            localStorage.setItem('modalClosed', 'true');
            // 关闭欢迎弹窗后，立即显示小弹窗
            createFloatingNotification();
        }
    });
    
    // 初始化粒子背景
    particlesJS('particles-js', {
        "particles": {
            "number": {
                "value": 80,
                "density": {
                    "enable": true,
                    "value_area": 800
                }
            },
            "color": {
                "value": "#ffffff"
            },
            "shape": {
                "type": "circle"
            },
            "opacity": {
                "value": 0.5,
                "random": true
            },
            "size": {
                "value": 3,
                "random": true
            },
            "line_linked": {
                "enable": true,
                "distance": 150,
                "color": "#ffffff",
                "opacity": 0.1,
                "width": 1
            },
            "move": {
                "enable": true,
                "speed": 1,
                "direction": "none",
                "random": true,
                "straight": false,
                "out_mode": "out",
                "bounce": false
            }
        },
        "interactivity": {
            "detect_on": "canvas",
            "events": {
                "onhover": {
                    "enable": true,
                    "mode": "grab"
                },
                "onclick": {
                    "enable": true,
                    "mode": "push"
                },
                "resize": true
            },
            "modes": {
                "grab": {
                    "distance": 140,
                    "line_linked": {
                        "opacity": 0.3
                    }
                },
                "push": {
                    "particles_nb": 4
                }
            }
        },
        "retina_detect": true
    });
    
    // 加载统计数据
    fetch('get_stats.php')
        .then(res => res.json())
        .then(data => {
            totalStats.textContent = `总成功破解量：${data.total || 0}`;
            todayStats.textContent = `今日成功破解量：${data.today || 0}`;
            ninjaStats.textContent = `忍者破解总数量：${data.ninja || 0}`;
            krnlStats.textContent = `krnl破解总数量：${data.krnl || 0}`;
        })
        .catch(err => console.log('统计初始化失败:', err));
    
    // 获取卡密按钮点击事件
    generateBtn.addEventListener('click', () => {
        const link = linkInput.value.trim();
        if (!link) {
            alert('请输入链接');
            return;
        }
        
        let processedLink = link;
        let isKrnlAndroid = false;
        let isKrnlIos = false;
        let isNinja = false;
        
        // 链接处理逻辑 - 增加了对 krnl-ios.com 的支持
        if (link.includes('deltaios-executor.com/ads.html?URL=') || link.includes('krnl-ios.com/ads.html?URL=')) {
            const urlMatch = link.match(/URL=([^&]+)/);
            if (urlMatch && urlMatch[1]) {
                try {
                    const decodedUrl = atob(urlMatch[1]);
                    processedLink = decodedUrl;
                    if (decodedUrl.startsWith('https://krnl.cat/checkpoint/ios/v1?hwid=')) {
                        isKrnlIos = true;
                    }
                    else if (decodedUrl.startsWith('https://krnl.cat/checkpoint/android/v1?hwid=')) {
                        isKrnlAndroid = true;
                    }
                } catch (e) {
                    alert('Base64解码失败，请检查链接');
                    return;
                }
            } else {
                alert('未找到URL参数');
                return;
            }
        } 
        else if (link.startsWith('https://krnl.cat/checkpoint/ios/v1?hwid=')) {
            isKrnlIos = true;
        }
        else if (link.startsWith('https://krnl.cat/checkpoint/android/v1?hwid=')) {
            isKrnlAndroid = true;
        }
        else if (link.startsWith('https://auth.platoboost.app/a?d=')) {
            isNinja = true;
        }
        else {
            alert('链接格式不正确，请使用支持的链接类型');
            return;
        }
        
        // 显示加载状态
        generateBtn.classList.add('loading');
        resultDiv.style.display = 'none';
        copyBtn.style.display = 'none';
        
        // 发送请求获取卡密
        fetch('extract_key.php', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `link=${encodeURIComponent(processedLink)}&isKrnlIos=${isKrnlIos}&isKrnlAndroid=${isKrnlAndroid}`
        })
        .then(res => res.json())
        .then(data => {
            generateBtn.classList.remove('loading');
            resultDiv.style.display = 'block';
            
            if (data.success && data.key) {
                // 显示成功信息
                resultContent.innerHTML = `提取成功！\n${isKrnlIos? 'iOS版卡密' : isKrnlAndroid? 'Android版卡密' : '卡密'}：\n<span class="success-key">${data.key}</span>`;
                copyBtn.style.display = 'block';
                
                // 更新统计数据
                let type = '';
                if (isKrnlIos || isKrnlAndroid) {
                    type = 'krnl';
                } else if (isNinja) {
                    type = 'ninja';
                }
                
                fetch('update_stats.php', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ key: 'ey886', type: type })
                })
                .then(res => res.json())
                .then(statsData => {
                    totalStats.textContent = `总成功破解量：${statsData.total}`;
                    todayStats.textContent = `今日成功破解量：${statsData.today}`;
                    ninjaStats.textContent = `忍者破解总数量：${statsData.ninja}`;
                    krnlStats.textContent = `krnl破解总数量：${statsData.krnl}`;
                    
                    // 统计数字动画效果
                    totalStats.style.transform = 'scale(1.1)';
                    todayStats.style.transform = 'scale(1.1)';
                    if (type === 'ninja') {
                        ninjaStats.style.transform = 'scale(1.1)';
                    } else if (type === 'krnl') {
                        krnlStats.style.transform = 'scale(1.1)';
                    }
                    
                    setTimeout(() => {
                        totalStats.style.transform = 'scale(1)';
                        todayStats.style.transform = 'scale(1)';
                        if (type === 'ninja') {
                            ninjaStats.style.transform = 'scale(1)';
                        } else if (type === 'krnl') {
                            krnlStats.style.transform = 'scale(1)';
                        }
                    }, 200);
                });
            } else {
                resultContent.innerHTML = `${data.message || '提取失败，请重试'}`;
            }
        })
        .catch(err => {
            generateBtn.classList.remove('loading');
            resultContent.innerHTML = `请求失败：${err.message}`;
            resultDiv.style.display = 'block';
        });
    });
    
    // 复制按钮点击事件
    copyBtn.addEventListener('click', () => {
        const keyElement = resultContent.querySelector('.success-key');
        if (keyElement) {
            const key = keyElement.textContent.trim();
            const textarea = document.createElement('textarea');
            textarea.value = key;
            document.body.appendChild(textarea);
            textarea.select();
            document.execCommand('copy');
            document.body.removeChild(textarea);
            
            // 复制成功反馈
            const originalHTML = copyBtn.innerHTML;
            copyBtn.innerHTML = '已复制';
            setTimeout(() => {
                copyBtn.innerHTML = originalHTML;
            }, 2000);
        }
    });
    
   
    function createFloatingNotification() {
        const notification = document.createElement('div');
        notification.className = 'floating-notification';
        
        const content = document.createElement('div');
        content.className = 'floating-notification-content';
        notification.appendChild(content);
        
        document.body.appendChild(notification);
        
       
        const jayLyrics = [
            '祝你天天开心'
        ];
        
       
        const randomText = jayLyrics[Math.floor(Math.random() * jayLyrics.length)];
        let index = 0;
        
        function typeWriter() {
            if (index < randomText.length) {
                content.textContent += randomText.charAt(index);
                index++;
                setTimeout(typeWriter, 100); 
            }
        }
        
        // 延迟一秒开始打字
        setTimeout(typeWriter, 1000);
        
        // 10秒后移除通知（独立计时器）
        setTimeout(() => {
            notification.style.opacity = '0';
            setTimeout(() => {
                notification.style.display = 'none';
            }, 500);
        }, 10000);
    }
});
