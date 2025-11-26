-- 增加论坛活跃度的额外帖子和评论数据
USE admitgenius_db;

-- 插入更多示例帖子让论坛更活跃
INSERT INTO forum_posts (
    author_id, title, content, category, like_count, comment_count, 
    is_expert_post, created_at
) VALUES 

-- 申请经验分享帖子
(
    (SELECT id FROM users WHERE email = 'student@test.com'), 
    '【25 Fall 申请经验】双非背景，GPA 3.5 如何拿下北美 Top 30 CS 硕士 offer？', 
    '各位志在远方的同学大家好！我是计算机学院21届的学长，刚刚结束了我的申请季，有幸拿到了几所北美CS项目的Offer，最终决定去UCSD。回想整个申请过程，充满了焦虑与不确定，但也收获满满。\n\n我的背景：本科武汉大学（非CS强相关专业），GPA 3.5/4.0，托福105+，GRE 325+，有两段国内大厂的实习经历，一段校内科研。\n\n核心经验分享：\n\n1. 尽早规划，明确方向： 大二下学期我就开始思考出国的事情，并且针对性地选修了一些CS相关的课程，弥补背景不足。\n\n2. 标化成绩是门槛： 托福和GRE一定要下大力气刷分，越高越好。建议提前一年开始准备。\n\n3. 科研/实习经历加分： 积极联系老师进实验室，或者寻找有含金量的实习，这对于文书和面试都非常有帮助。\n\n4. 文书打磨至关重要： PS和CV一定要反复修改，突出自己的亮点和与项目的匹配度。可以找有经验的学长学姐或者机构帮忙润色。\n\n5. 选校策略： 拉开梯度，彩票、主申、保底都要有。多看官网信息，了解项目特色和招生偏好。\n\n希望我的经历能给大家一些启发和信心！申请季很漫长，保持心态，祝大家都能拿到梦校的offer！有问题欢迎在评论区交流。', 
    'experience',
    28, 
    0, 
    FALSE, 
    DATE_SUB(NOW(), INTERVAL 2 DAY)
),

-- 欧洲留学指南帖子
(
    (SELECT id FROM users WHERE email = 'student2@test.com'), 
    '【欧洲留学党集合】不同国家/地区的留学费用及奖学金申请指南（持续更新）', 
    '大家好！计划去欧洲留学的同学们看过来！欧洲国家众多，教育体系各具特色，留学费用和奖学金政策也大相径庭。这个帖子旨在为大家梳理不同热门欧洲留学国家/地区的费用情况和主要的奖学金申请途径。\n\n主要国家/地区概览（以硕士为例，仅供参考）：\n\n🇬🇧英国：\n学费：文商科2-3万英镑/年，理工科2.5-4万英镑/年。\n生活费：伦敦地区1.2-1.5万英镑/年，非伦敦地区0.8-1.2万英镑/年。\n主要奖学金：志奋领奖学金 (Chevening Scholarships)，各类大学奖学金。\n\n🇩🇪德国：\n公立大学大部分专业免学费（巴符州等少数州对非欧盟学生收费）。\n生活费：约800-1000欧元/月（包含强制保险）。\n主要奖学金：DAAD奖学金，德国各基金会奖学金。\n\n🇫🇷法国：\n公立大学注册费较低（几百欧元），私立院校和高商学费较高。\n生活费：巴黎地区1000-1200欧元/月，外省600-800欧元/月。\n主要奖学金：埃菲尔奖学金 (Eiffel Excellence Scholarship)，法国政府奖学金。\n\n🇳🇱荷兰、🇨🇭瑞士、北欧等国家：费用和政策各不相同，建议具体查询。\n\n通用奖学金申请Tips：\n✅ 提前关注学校官网和国家留学基金委（CSC）信息\n✅ 注意申请截止日期，材料准备要充分\n✅ 个人陈述中体现与奖学金要求的契合度\n\n欢迎大家在评论区补充和提问，我会持续更新本帖内容！', 
    'life',
    35, 
    0, 
    FALSE, 
    DATE_SUB(NOW(), INTERVAL 3 DAY)
),

-- 推荐信教程帖子
(
    (SELECT id FROM users WHERE email = 'student3@test.com'), 
    '【保姆级教程】手把手教你搞定留学推荐信！附模板和常见问题解答', 
    '各位同学，推荐信（RL）在留学申请中的重要性不言而喻。一封强有力的推荐信能让招生官对你刮目相看。但是如何邀请老师写推荐信？老师太忙没时间怎么办？推荐信的内容怎么写才能突出优势？\n\n别担心，这篇帖子为你一一解答！\n\n📝 一、邀请推荐人：\n\n1. 选择合适的推荐人： 优先选择教过你专业课、指导过你科研、或对你比较了解的教授或副教授。职场推荐信则找直属上司。\n\n2. 提前沟通： 至少提前1-2个月联系老师，说明你的留学意向、申请的学校和专业，并询问老师是否愿意为你写推荐信。\n\n3. 提供充足的材料： 包括你的CV、成绩单、个人陈述（PS初稿）、以及你想在推荐信中强调的亮点和事例。\n\n✍️ 二、推荐信的内容（可以和老师商议或自己提供初稿）：\n\n• 推荐人与你的关系，认识多久，通过什么方式认识\n• 你的学术能力、科研潜力、学习态度、创新思维等\n• 结合具体事例来支撑观点（非常重要！）\n• 你与所申请项目的匹配度\n• 推荐人对你未来发展的期望\n\n❓ 三、常见问题Q&A：\n\nQ: 老师太忙，让我自己写初稿怎么办？\nA: 这是很常见的情况。认真撰写，然后请老师修改确认。\n\nQ: 需要几封推荐信？\nA: 一般是2-3封，具体看学校要求。\n\nQ: 推荐信需要通过网申系统提交吗？\nA: 大部分是的，老师会收到学校发来的链接进行上传。\n\n希望这份教程能帮助大家顺利搞定推荐信！', 
    'experience',
    24, 
    0, 
    FALSE, 
    DATE_SUB(NOW(), INTERVAL 4 DAY)
);

-- 插入专家帖子（院校和考试指导）
INSERT INTO forum_posts (
    author_id, title, content, category, like_count, comment_count, 
    is_expert_post, expert_tag, created_at
) VALUES 

-- 专家学校分析帖子
(
    (SELECT id FROM users WHERE email = 'expert2@test.com'), 
    '【专家解析】2025年英国G5大学申请趋势及录取策略分析', 
    '各位同学大家好！作为专业的留学顾问，我想跟大家分析一下2025年英国G5大学（牛津、剑桥、IC、UCL、LSE）的申请趋势和录取策略。\n\n📊 2025申请趋势总览：\n\n1. **申请人数持续增长**\n- 中国学生申请量同比增长15%\n- 竞争更加激烈，录取率进一步下降\n- 早申请的优势更加明显\n\n2. **录取标准变化**\n- 更注重综合素质而非单纯的成绩\n- 软背景（实习、科研、竞赛）权重增加\n- 面试环节更加重要\n\n🎯 各校录取策略解析：\n\n**牛津大学：**\n- 依然看重学术成绩，但更关注学术潜力\n- 面试是关键，需要展现批判性思维\n- 推荐信质量要求极高\n\n**剑桥大学：**\n- 学科竞赛获奖经历加分明显\n- 研究经历和学术论文是亮点\n- 入学考试准备要充分\n\n**帝国理工：**\n- 理工科背景要求严格\n- 实习和项目经验很重要\n- 英语要求相对较高\n\n**UCL & LSE：**\n- 专业匹配度要求高\n- 个人陈述需要体现明确的职业规划\n- 工作经验对某些专业很重要\n\n💡 申请建议：\n\n1. **提早规划**：至少提前1.5年开始准备\n2. **背景提升**：积极参与科研、实习、竞赛\n3. **文书质量**：个人陈述要体现独特性和深度\n4. **面试准备**：多练习，展现学术热情\n\n如果大家有具体的申请问题，欢迎私信咨询或在评论区讨论！', 
    'schools',
    38, 
    0, 
    TRUE, 
    '院校分析', 
    DATE_SUB(NOW(), INTERVAL 2 DAY)
),

-- 专家考试指导帖子
(
    (SELECT id FROM users WHERE email = 'expert3@test.com'), 
    '【考试指导】托福115+备考策略：从基础到高分的完整路径', 
    '同学们好！作为语言培训专家，我经常收到关于托福备考的咨询。今天分享一套从基础到高分的完整备考策略，帮助大家系统性地提升托福成绩。\n\n🎯 目标设定与时间规划：\n\n**基础阶段（2-3个月）：**\n- 词汇量积累至8000+\n- 语法基础巩固\n- 听说读写四项基础练习\n- 目标分数：70-80分\n\n**提升阶段（2-3个月）：**\n- 专项技能强化训练\n- 模拟考试练习\n- 弱项针对性突破\n- 目标分数：85-100分\n\n**冲刺阶段（1-2个月）：**\n- 高频模考+错题分析\n- 考试技巧优化\n- 心理状态调节\n- 目标分数：100+分\n\n📚 各科目备考重点：\n\n**阅读（目标28+）：**\n- 词汇是基础，重点掌握学术词汇\n- 练习长难句分析\n- 熟悉题型和解题技巧\n- 控制做题时间，提高效率\n\n**听力（目标28+）：**\n- 精听+泛听结合\n- 熟悉美式英语发音特点\n- 练习记笔记技巧\n- 重点关注讲座类材料\n\n**口语（目标26+）：**\n- 模板练习+个性化表达\n- 发音清晰度训练\n- 逻辑结构优化\n- 大量练习提高流利度\n\n**写作（目标28+）：**\n- 综合写作：听力能力+模板应用\n- 独立写作：逻辑清晰+论证充分\n- 语法准确性检查\n- 词汇多样性提升\n\n⚡ 高分技巧总结：\n\n1. **制定详细的备考计划**\n2. **坚持每日练习，保持语感**\n3. **定期模考，检验效果**\n4. **针对性强化弱项**\n5. **考前状态调整**\n\n记住，托福备考是一个系统工程，需要持续的努力和正确的方法。祝大家都能取得理想的成绩！', 
    'exams',
    42, 
    0, 
    TRUE, 
    '考试指导', 
    DATE_SUB(NOW(), INTERVAL 3 DAY)
);

-- 插入评论数据
INSERT INTO comments (post_id, author_id, content, like_count, created_at) VALUES 
-- 对CS申请经验帖子的评论
((SELECT id FROM forum_posts WHERE title LIKE '%双非背景，GPA 3.5%'), (SELECT id FROM users WHERE email = 'student2@test.com'), '感谢学长分享！我目前大二，也想申请CS，但是感觉科研好难找，有什么建议吗？', 5, DATE_SUB(NOW(), INTERVAL 1 DAY)),

((SELECT id FROM forum_posts WHERE title LIKE '%双非背景，GPA 3.5%'), (SELECT id FROM users WHERE email = 'student3@test.com'), '学长太强了！请问文书大概修改了多少遍呀？有没有推荐的润色渠道？', 3, DATE_SUB(NOW(), INTERVAL 1 DAY)),

-- 专家对CS申请帖的指导评论
((SELECT id FROM forum_posts WHERE title LIKE '%双非背景，GPA 3.5%'), (SELECT id FROM users WHERE email = 'advisor@test.com'), '恭喜这位同学！作为专业顾问，我想补充几点：1. 科研经历虽然重要，但质量比数量更关键；2. 选校时要充分考虑项目特色和自己的背景匹配度；3. 面试准备也很重要，建议提前了解常见问题。', 12, DATE_SUB(NOW(), INTERVAL 1 DAY)),

-- 对欧洲留学指南的评论
((SELECT id FROM forum_posts WHERE title LIKE '%欧洲留学党%'), (SELECT id FROM users WHERE email = 'student@test.com'), '太实用了！正在纠结德国还是荷兰，这个对比很清晰！', 4, DATE_SUB(NOW(), INTERVAL 2 DAY)),

((SELECT id FROM forum_posts WHERE title LIKE '%欧洲留学党%'), (SELECT id FROM users WHERE email = 'student3@test.com'), '请问楼主，CSC奖学金申请难度大吗？对本科院校和GPA有硬性要求吗？', 2, DATE_SUB(NOW(), INTERVAL 2 DAY)),

-- 专家对欧洲留学的专业建议
((SELECT id FROM forum_posts WHERE title LIKE '%欧洲留学党%'), (SELECT id FROM users WHERE email = 'expert2@test.com'), '补充一点关于德国的信息：虽然学费免费，但德语要求不容忽视。即使是英语授课项目，掌握基础德语对生活很有帮助。另外，德国的工签政策对留学生很友好，毕业后有18个月的找工作签证。', 8, DATE_SUB(NOW(), INTERVAL 2 DAY)),

-- 对推荐信教程的评论
((SELECT id FROM forum_posts WHERE title LIKE '%推荐信%'), (SELECT id FROM users WHERE email = 'student@test.com'), '干货满满！正愁怎么跟老师开口呢，邮件模板太有用了！', 6, DATE_SUB(NOW(), INTERVAL 3 DAY)),

((SELECT id FROM forum_posts WHERE title LIKE '%推荐信%'), (SELECT id FROM users WHERE email = 'student2@test.com'), '学长/学姐，如果想让老师强调我的某个科研项目，是直接在给老师的材料里写明吗？', 3, DATE_SUB(NOW(), INTERVAL 3 DAY)),

-- 对签证攻略的评论（为现有签证帖子）
((SELECT id FROM forum_posts WHERE title LIKE '%F1学生签证%'), (SELECT id FROM users WHERE email = 'student@test.com'), '陈顾问的指导太详细了！想问一下，如果被Check大概要等多久呀？', 7, DATE_SUB(NOW(), INTERVAL 1 DAY)),

((SELECT id FROM forum_posts WHERE title LIKE '%F1学生签证%'), (SELECT id FROM users WHERE email = 'student2@test.com'), '请问专家，面试时如果紧张说错话了怎么办？可以修正吗？', 4, DATE_SUB(NOW(), INTERVAL 1 DAY)),

-- 专家回复签证相关问题
((SELECT id FROM forum_posts WHERE title LIKE '%F1学生签证%'), (SELECT id FROM users WHERE email = 'advisor@test.com'), '关于Check的问题：一般需要2-8周，个别情况可能更长。被Check不代表被拒，保持耐心即可。关于面试紧张的问题：可以礼貌地请求重新组织语言，签证官通常会理解的。最重要的是保持诚实和自信。', 15, DATE_SUB(NOW(), INTERVAL 1 DAY)),

-- 对英国G5分析的评论
((SELECT id FROM forum_posts WHERE title LIKE '%G5大学申请趋势%'), (SELECT id FROM users WHERE email = 'student@test.com'), '专家分析得太专业了！请问IC的计算机专业今年竞争如何？', 5, DATE_SUB(NOW(), INTERVAL 1 DAY)),

((SELECT id FROM forum_posts WHERE title LIKE '%G5大学申请趋势%'), (SELECT id FROM users WHERE email = 'student3@test.com'), '想申请LSE的经济学，请问专家有什么特别的建议吗？', 3, DATE_SUB(NOW(), INTERVAL 1 DAY)),

-- 对托福指导的评论
((SELECT id FROM forum_posts WHERE title LIKE '%托福115+%'), (SELECT id FROM users WHERE email = 'student2@test.com'), '专家老师的方法太系统了！想问一下口语部分有什么快速提升的技巧吗？', 6, DATE_SUB(NOW(), INTERVAL 2 DAY)),

((SELECT id FROM forum_posts WHERE title LIKE '%托福115+%'), (SELECT id FROM users WHERE email = 'student@test.com'), '听力一直是我的弱项，请问专家有什么精听材料推荐吗？', 4, DATE_SUB(NOW(), INTERVAL 2 DAY));

-- 更新帖子的评论数量
UPDATE forum_posts SET comment_count = (
    SELECT COUNT(*) FROM comments WHERE comments.post_id = forum_posts.id
);

-- 提交更改
COMMIT; 