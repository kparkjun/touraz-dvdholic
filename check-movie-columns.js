const mysql = require("mysql2/promise");

const config = {
  host: process.env.DB_HOST || "z37udk8g6jiaqcbx.cbetxkdyhwsb.us-east-1.rds.amazonaws.com",
  user: process.env.DB_USERNAME || "b4nhfff518uodjsc",
  password: process.env.DB_PASSWORD || "wm1a5ni4mvh1euge",
  database: process.env.DB_NAME || "wjbn4crxvsx7nnlb",
  port: parseInt(process.env.DB_PORT || "3306", 10),
};

async function run() {
  const conn = await mysql.createConnection(config);
  const [rows] = await conn.execute(`
    SELECT
      content_type,
      COUNT(*) AS total,
      SUM(CASE WHEN director IS NOT NULL AND TRIM(director) <> '' AND director <> 'NOT_FOUND' THEN 1 ELSE 0 END) AS director_filled,
      SUM(CASE WHEN cast IS NOT NULL AND TRIM(cast) <> '' AND cast <> 'NOT_FOUND' THEN 1 ELSE 0 END) AS cast_filled,
      SUM(CASE WHEN runtime IS NOT NULL AND runtime <> 0 THEN 1 ELSE 0 END) AS runtime_filled,
      SUM(CASE WHEN budget IS NOT NULL AND budget <> 0 THEN 1 ELSE 0 END) AS budget_filled,
      SUM(CASE WHEN revenue IS NOT NULL AND revenue <> 0 THEN 1 ELSE 0 END) AS revenue_filled,
      SUM(CASE WHEN trailer_url IS NOT NULL AND TRIM(trailer_url) <> '' AND trailer_url <> 'NOT_FOUND' THEN 1 ELSE 0 END) AS trailer_filled,
      SUM(CASE WHEN ott_providers IS NOT NULL AND TRIM(ott_providers) <> '' AND ott_providers <> 'NOT_FOUND' THEN 1 ELSE 0 END) AS ott_filled,
      SUM(CASE WHEN collection_name IS NOT NULL AND TRIM(collection_name) <> '' AND collection_name <> 'NOT_FOUND' THEN 1 ELSE 0 END) AS collection_filled,
      SUM(CASE WHEN recommendations IS NOT NULL AND TRIM(recommendations) <> '' AND recommendations <> 'NOT_FOUND' THEN 1 ELSE 0 END) AS recommendations_filled,
      SUM(CASE WHEN top_review IS NOT NULL AND TRIM(top_review) <> '' AND top_review <> 'NOT_FOUND' THEN 1 ELSE 0 END) AS top_review_filled
    FROM movies
    GROUP BY content_type
    ORDER BY content_type
  `);

  console.log(JSON.stringify(rows, null, 2));
  await conn.end();
}

run().catch((e) => {
  console.error(e.message);
  process.exit(1);
});
