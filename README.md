# RagTestByUsingJava
一次尝试，应用了Spring AI，实现了智能对话功能，尝试应用了RAG，Agent，工具调用，MCP等技术，供之后学习参考

## 具体信息
- MCP调用那里尚不完善，因为要申请密钥什么的有丶小麻烦，但是看了一下具体的操作和工具调用（ToolCall）很像
- 实现了上下文的记忆
- RAG的知识检索数据库用的PGvector，调用了aliyun的api，存储在云端，在使用RAG检索的时候后端调用Embedding模型对查询进行向量化，然后通过向量查询数据库，返回相似结果（文档）加入到上下文供大模型参考
  - 这里还是prompt工程和ETL方法多一点，但是这个项目用于验证就不搞那么麻烦了（嘿嘿其实是我偷懒^_^）
- 前端是用Cursor写的（哇！AI简直太好用了你们知道吗.jpg），具体还是Vue3的技术
- 需要扩展的功能还有很多，但是可能不会在这个项目扩展了（挖坑）

## 实际效果
### 主页
<img width="2175" height="1356" alt="2cc9ef1add33e7484946fa73a49b10c8" src="https://github.com/user-attachments/assets/e80aa222-ad78-4bde-a97f-935b50914d15" />

### 问答
<img width="2181" height="1347" alt="2092e1bbff095baa30b04db1f6e1318b" src="https://github.com/user-attachments/assets/64545bf0-9149-4274-9a49-3a26a2e3e7cc" />


<img width="2175" height="1353" alt="74451ca1f882c1d06455b5928c38647c" src="https://github.com/user-attachments/assets/0299fe64-69f2-4d0b-8fda-fcee03194ff0" />


*写在最后：哇，'革命道中 - On The Way'——アイナ・ジ・エンド简直太好听了，简直有丶 夢中になる 了*
