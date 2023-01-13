agent-dyn 和 agent-with-dependencies，agentconfig 需要放在server同级


热更步骤
1：将server.jar 上传
2：上传热更class名到 agentconfig文件下
3：执行agent-with-dependencies热更



特别注意：
只能够热更方法内的改动
不能够删除，增加方法，不能够修改静态变量
