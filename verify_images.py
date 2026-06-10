"""验证三份 docx 中嵌入的图片数量"""
import os
from docx import Document

REPORTS_DIR = r"F:\Desktop\个人资料\学校作业\大三下学期作业\计算机设计大赛\卷积核微课\框架\实验报告"

for fname in ['实验报告1-Fabric.js卷积核可视化交互.docx',
              '实验报告2-Three.js 3D化作品展示.docx',
              '实验报告3-ECharts数据可视化.docx']:
    path = os.path.join(REPORTS_DIR, fname)
    doc = Document(path)
    images = [r for r in doc.part.rels.values() if 'image' in r.target_ref]
    paragraphs = len(doc.paragraphs)
    print(f"{fname}: 图片数={len(images)}, 段落数={paragraphs}")
