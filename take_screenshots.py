"""使用 Playwright 截取三个页面在不同状态下的截图"""
import asyncio
import os
from playwright.async_api import async_playwright

BASE = "http://127.0.0.1:9000/static/pages"
OUT = r"F:\Desktop\个人资料\学校作业\大三下学期作业\计算机设计大赛\卷积核微课\框架\screenshots"

os.makedirs(OUT, exist_ok=True)


async def shoot_playground(page):
    url = f"{BASE}/playground.html"
    await page.set_viewport_size({"width": 1280, "height": 820})
    await page.goto(url, wait_until="networkidle")
    await page.wait_for_timeout(800)

    # 整体界面 (overview)
    await page.screenshot(path=os.path.join(OUT, "playground-overview.png"), full_page=False)

    # Sobel-X 边缘检测
    await page.click('button[data-preset="sobelX"]')
    await page.wait_for_timeout(300)
    await page.click('#btnApply')
    await page.wait_for_timeout(500)
    await page.screenshot(path=os.path.join(OUT, "playground-sobelx.png"), full_page=False)

    # 浮雕效果
    await page.click('button[data-preset="emboss"]')
    await page.wait_for_timeout(300)
    await page.click('#btnApply')
    await page.wait_for_timeout(500)
    await page.screenshot(path=os.path.join(OUT, "playground-emboss.png"), full_page=False)

    # 锐化效果
    await page.click('button[data-preset="sharpen"]')
    await page.wait_for_timeout(300)
    await page.click('#btnApply')
    await page.wait_for_timeout(500)
    await page.screenshot(path=os.path.join(OUT, "playground-sharpen.png"), full_page=False)

    # 边缘检测（更经典）
    await page.click('button[data-preset="edge"]')
    await page.wait_for_timeout(300)
    await page.click('#btnApply')
    await page.wait_for_timeout(500)
    await page.screenshot(path=os.path.join(OUT, "playground-edge.png"), full_page=False)

    # 自由涂鸦：在原图画布上画几条线，然后应用拉普拉斯
    canvas = await page.query_selector('#srcCanvas')
    box = await canvas.bounding_box()
    # 切换到画笔
    await page.click('#t-pen')
    await page.wait_for_timeout(200)
    # 画出几道线（鼠标按下移动）
    cx, cy = box['x'] + 80, box['y'] + 100
    await page.mouse.move(cx, cy)
    await page.mouse.down()
    for i in range(20):
        await page.mouse.move(cx + i * 8, cy + (i % 4) * 5)
    await page.mouse.up()
    cx2, cy2 = box['x'] + 100, box['y'] + 200
    await page.mouse.move(cx2, cy2)
    await page.mouse.down()
    for i in range(20):
        await page.mouse.move(cx2 + i * 6, cy2 - (i % 5) * 4)
    await page.mouse.up()
    await page.wait_for_timeout(300)
    # 应用拉普拉斯查看涂鸦效果
    await page.click('button[data-preset="laplacian"]')
    await page.wait_for_timeout(300)
    await page.click('#btnApply')
    await page.wait_for_timeout(500)
    await page.screenshot(path=os.path.join(OUT, "playground-brush.png"), full_page=False)


async def shoot_showcase(page):
    url = f"{BASE}/showcase-3d.html"
    await page.set_viewport_size({"width": 1280, "height": 820})
    await page.goto(url, wait_until="networkidle")
    await page.wait_for_timeout(1500)

    # 默认 3D 卷积核场景
    await page.screenshot(path=os.path.join(OUT, "showcase-kernel.png"), full_page=False)

    # 滑窗动画场景
    await page.click('button[data-scene="sliding"]')
    await page.wait_for_timeout(1500)
    await page.screenshot(path=os.path.join(OUT, "showcase-sliding.png"), full_page=False)

    # 特征图金字塔
    await page.click('button[data-scene="featuremap"]')
    await page.wait_for_timeout(1200)
    await page.screenshot(path=os.path.join(OUT, "showcase-pyramid.png"), full_page=False)

    # 鼠标悬浮场景
    await page.click('button[data-scene="hover"]')
    await page.wait_for_timeout(1200)
    # 模拟悬浮于场景中央
    await page.mouse.move(640, 400)
    await page.wait_for_timeout(500)
    await page.screenshot(path=os.path.join(OUT, "showcase-hover.png"), full_page=False)

    # 线框模式：回到 3D 卷积核场景
    await page.click('button[data-scene="kernel"]')
    await page.wait_for_timeout(1200)
    # 寻找线框按钮（文本含"线框"）
    wf = await page.query_selector('button:has-text("线框")')
    if wf:
        await wf.click()
        await page.wait_for_timeout(800)
    await page.screenshot(path=os.path.join(OUT, "showcase-wireframe.png"), full_page=False)


async def shoot_dataviz(page):
    url = f"{BASE}/data-viz.html"
    await page.set_viewport_size({"width": 1280, "height": 1800})
    await page.goto(url, wait_until="networkidle")
    await page.wait_for_timeout(2500)

    # 数据集 1 - 全页面（顶部+饼图+雷达）
    await page.screenshot(path=os.path.join(OUT, "dataviz-ds1-overview.png"), full_page=True)

    # 数据集 1 - 饼图 + 雷达（视口内）
    await page.set_viewport_size({"width": 1280, "height": 820})
    await page.evaluate("window.scrollTo(0, 360)")
    await page.wait_for_timeout(600)
    await page.screenshot(path=os.path.join(OUT, "dataviz-ds1-pie-radar.png"), full_page=False)

    # 数据集 1 - 复杂图表（多维联动柱状+折线）
    await page.evaluate("document.getElementById('chart3').scrollIntoView({block:'center'})")
    await page.wait_for_timeout(600)
    await page.screenshot(path=os.path.join(OUT, "dataviz-ds1-complex.png"), full_page=False)

    # 切换到数据集 2 - 卷积核应用
    await page.evaluate("window.scrollTo(0,0)")
    await page.wait_for_timeout(400)
    await page.click('button[data-ds="kernel"]')
    await page.wait_for_timeout(2000)

    await page.set_viewport_size({"width": 1280, "height": 1800})
    await page.screenshot(path=os.path.join(OUT, "dataviz-ds2-overview.png"), full_page=True)

    await page.set_viewport_size({"width": 1280, "height": 820})
    await page.evaluate("window.scrollTo(0, 360)")
    await page.wait_for_timeout(600)
    await page.screenshot(path=os.path.join(OUT, "dataviz-ds2-pie-radar.png"), full_page=False)

    # 中国地图
    await page.evaluate("document.getElementById('chart3').scrollIntoView({block:'center'})")
    await page.wait_for_timeout(1500)
    await page.screenshot(path=os.path.join(OUT, "dataviz-ds2-map.png"), full_page=False)


async def main():
    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True)
        ctx = await browser.new_context()
        page = await ctx.new_page()

        print("== Playground ==")
        await shoot_playground(page)
        print("== Showcase 3D ==")
        await shoot_showcase(page)
        print("== Data Viz ==")
        await shoot_dataviz(page)

        await browser.close()
    print("Done")


if __name__ == "__main__":
    asyncio.run(main())
