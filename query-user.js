// 임시: 박옥남 검색 후 삭제
const host = process.env.DB_HOST || 'z37udk8g6jiaqcbx.cbetxkdyhwsb.us-east-1.rds.amazonaws.com';
const user = process.env.DB_USERNAME || 'b4nhfff518uodjsc';
const password = process.env.DB_PASSWORD || 'wm1a5ni4mvh1euge';
const database = process.env.DB_NAME || 'wjbn4crxvsx7nnlb';
const port = parseInt(process.env.DB_PORT || '3306', 10);

async function run() {
  const mysql = await import('mysql2/promise');
  const conn = await mysql.default.createConnection({ host, port, user, password, database });
  const [rows1] = await conn.execute("SELECT USER_ID, USER_NAME, EMAIL FROM users WHERE USER_NAME = ?", ['박옥남']);
  const [rows2] = await conn.execute("SELECT SOCIAL_USER_ID, USER_NAME, PROVIDER FROM social_users WHERE USER_NAME = ?", ['박옥남']);
  await conn.end();
  const inUsers = rows1.length > 0;
  const inSocial = rows2.length > 0;
  console.log('=== 박옥남 검색 결과 ===');
  if (inUsers) console.log('users:', JSON.stringify(rows1, null, 2));
  if (inSocial) console.log('social_users:', JSON.stringify(rows2, null, 2));
  if (!inUsers && !inSocial) console.log('없음 (일반/소셜 회원 모두 해당 이름 없음)');
}

run().catch(e => { console.error(e.message); process.exit(1); });
