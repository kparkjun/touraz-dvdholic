// 임시: DB 테이블 목록 및 구조 조회 (사용 후 삭제)
const host = process.env.DB_HOST || 'z37udk8g6jiaqcbx.cbetxkdyhwsb.us-east-1.rds.amazonaws.com';
const user = process.env.DB_USERNAME || 'b4nhfff518uodjsc';
const password = process.env.DB_PASSWORD || 'wm1a5ni4mvh1euge';
const database = process.env.DB_NAME || 'wjbn4crxvsx7nnlb';
const port = parseInt(process.env.DB_PORT || '3306', 10);

async function run() {
  const mysql = await import('mysql2/promise');
  const conn = await mysql.default.createConnection({ host, port, user, password, database });

  const [tableRows] = await conn.execute("SHOW TABLES");
  const tableKey = `Tables_in_${database}`;
  const tables = tableRows.map((r) => r[tableKey]);

  console.log('=== DB 테이블 목록 ===\n');
  for (const table of tables) {
    const [cols] = await conn.execute(`DESCRIBE \`${table}\``);
    const [countRows] = await conn.execute(`SELECT COUNT(*) as c FROM \`${table}\``);
    const count = countRows[0].c;
    console.log(`[${table}] (행 수: ${count})`);
    console.log(cols.map((c) => `  ${c.Field}  ${c.Type}  ${c.Null}  ${c.Key}  ${c.Default || ''}`).join('\n'));
    console.log('');
  }

  await conn.end();
}

run().catch((e) => {
  console.error(e.message);
  process.exit(1);
});
