const axios = require('axios');

// 测试登录功能
async function testLogin(username, password) {
  console.log(`\n===== 测试用户: ${username}, 密码: ${password} =====`);
  
  const startTime = Date.now();
  
  try {
    const response = await axios.post('http://localhost:8082/api/auth/login', {
      username: username,
      password: password
    }, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
    
    const endTime = Date.now();
    console.log(`✅ 登录成功! (耗时: ${endTime - startTime}ms)`);
    console.log('响应数据:', JSON.stringify(response.data, null, 2));
    return true;
  } catch (error) {
    const endTime = Date.now();
    console.log(`❌ 登录失败! (耗时: ${endTime - startTime}ms)`);
    console.log('错误状态码:', error.response ? error.response.status : '无响应');
    console.log('错误数据:', error.response ? JSON.stringify(error.response.data, null, 2) : error.message);
    return false;
  }
}

// 批量测试多个用户
async function runBatchTests() {
  console.log('开始批量测试登录功能...');
  
  const testUsers = [
    { username: 'admin', password: '123456' },
    { username: 'zhang_counselor', password: '123456' },
    { username: 'student1', password: '123456' },
    { username: 'admin', password: 'admin123' } // 尝试一个可能错误的密码
  ];
  
  let successCount = 0;
  let failCount = 0;
  
  for (const user of testUsers) {
    const success = await testLogin(user.username, user.password);
    if (success) successCount++;
    else failCount++;
  }
  
  console.log(`\n===== 测试结果摘要 =====`);
  console.log(`总测试: ${testUsers.length}, 成功: ${successCount}, 失败: ${failCount}`);
  
  // 测试健康状态接口
  try {
    console.log('\n测试健康状态接口...');
    const healthResponse = await axios.get('http://localhost:8082/api/test/health');
    console.log('健康状态接口响应:', healthResponse.data);
  } catch (error) {
    console.log('健康状态接口调用失败:', error.message);
  }
}

// 运行测试
runBatchTests().catch(err => {
  console.error('测试过程中出现错误:', err.message);
});

