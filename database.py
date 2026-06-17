"""MySQL 数据库连接管理（aiomysql 连接池 + 自动建库建表）"""
import os
import aiomysql
from dotenv import load_dotenv

load_dotenv()

MYSQL_HOST = os.getenv("MYSQL_HOST", "localhost")
MYSQL_PORT = int(os.getenv("MYSQL_PORT", "3306"))
MYSQL_USER = os.getenv("MYSQL_USER", "root")
MYSQL_PASSWORD = os.getenv("MYSQL_PASSWORD", "")
MYSQL_DB = os.getenv("MYSQL_DB", "vlab")

_pool: aiomysql.Pool | None = None

USER_DATA_KEYS = [
    "convolutionKernelNotes",
    "vlab_home_chat",
    "convolution_notes",
    "convolution_wrong_questions",
    "vlab_study_chat",
    "vlab_tutor_chat",
]

CREATE_DB_SQL = f"CREATE DATABASE IF NOT EXISTS `{MYSQL_DB}` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"

CREATE_TABLES_SQL = [
    """CREATE TABLE IF NOT EXISTS user_data (
        id INT AUTO_INCREMENT PRIMARY KEY,
        user_id VARCHAR(50) NOT NULL DEFAULT 'default',
        data_key VARCHAR(100) NOT NULL,
        data_value LONGTEXT,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        UNIQUE KEY uk_user_key (user_id, data_key)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""",
    """CREATE TABLE IF NOT EXISTS chat_messages (
        id INT AUTO_INCREMENT PRIMARY KEY,
        user_id VARCHAR(50) NOT NULL DEFAULT 'default',
        context VARCHAR(20) NOT NULL,
        role VARCHAR(10) NOT NULL,
        content TEXT NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        INDEX idx_user_context (user_id, context)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""",
]


async def get_pool() -> aiomysql.Pool:
    global _pool
    if _pool is None:
        _pool = await aiomysql.create_pool(
            host=MYSQL_HOST,
            port=MYSQL_PORT,
            user=MYSQL_USER,
            password=MYSQL_PASSWORD,
            db=MYSQL_DB,
            autocommit=True,
            minsize=1,
            maxsize=5,
        )
    return _pool


async def init_db():
    """建库 + 建表，程序启动时调用一次"""
    conn: aiomysql.Connection = await aiomysql.connect(
        host=MYSQL_HOST,
        port=MYSQL_PORT,
        user=MYSQL_USER,
        password=MYSQL_PASSWORD,
        autocommit=True,
    )
    try:
        async with conn.cursor() as cur:
            await cur.execute(CREATE_DB_SQL)
            await cur.execute(f"USE `{MYSQL_DB}`")
            for sql in CREATE_TABLES_SQL:
                await cur.execute(sql)
    finally:
        conn.close()


async def close_pool():
    global _pool
    if _pool:
        _pool.close()
        await _pool.wait_closed()
        _pool = None


# ---- user_data CRUD ----

async def save_user_data(user_id: str, data_key: str, data_value: str):
    pool = await get_pool()
    async with pool.acquire() as conn:
        async with conn.cursor() as cur:
            await cur.execute(
                "INSERT INTO user_data (user_id, data_key, data_value) VALUES (%s, %s, %s) "
                "ON DUPLICATE KEY UPDATE data_value = VALUES(data_value)",
                (user_id, data_key, data_value),
            )


async def load_user_data(user_id: str) -> dict[str, str]:
    pool = await get_pool()
    async with pool.acquire() as conn:
        async with conn.cursor(aiomysql.DictCursor) as cur:
            await cur.execute(
                "SELECT data_key, data_value FROM user_data WHERE user_id = %s",
                (user_id,),
            )
            rows = await cur.fetchall()
            return {row["data_key"]: row["data_value"] for row in rows}


# ---- chat_messages CRUD ----

async def save_chat_message(user_id: str, context: str, role: str, content: str):
    pool = await get_pool()
    async with pool.acquire() as conn:
        async with conn.cursor() as cur:
            await cur.execute(
                "INSERT INTO chat_messages (user_id, context, role, content) VALUES (%s, %s, %s, %s)",
                (user_id, context, role, content),
            )


async def sync_chat_messages(user_id: str, context: str, messages: list[dict]):
    """全量替换聊天记录：先删后插"""
    pool = await get_pool()
    async with pool.acquire() as conn:
        async with conn.cursor() as cur:
            await cur.execute(
                "DELETE FROM chat_messages WHERE user_id = %s AND context = %s",
                (user_id, context),
            )
            for m in messages:
                await cur.execute(
                    "INSERT INTO chat_messages (user_id, context, role, content) VALUES (%s, %s, %s, %s)",
                    (user_id, context, m.get("role", "user"), m.get("content", "")),
                )


async def load_chat_messages(user_id: str, context: str) -> list[dict]:
    pool = await get_pool()
    async with pool.acquire() as conn:
        async with conn.cursor(aiomysql.DictCursor) as cur:
            await cur.execute(
                "SELECT role, content, created_at FROM chat_messages WHERE user_id = %s AND context = %s ORDER BY id ASC",
                (user_id, context),
            )
            rows = await cur.fetchall()
            return [{"role": r["role"], "content": r["content"]} for r in rows]
