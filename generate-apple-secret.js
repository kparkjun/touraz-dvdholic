const fs = require("fs");
const path = require("path");

async function main() {
  const { importPKCS8, SignJWT } = await import("jose");

  const keyPath = path.join(__dirname, "AuthKey_KY9B5XN6WQ.p8");
  let privateKeyPem = fs.readFileSync(keyPath, "utf8");
  privateKeyPem = privateKeyPem
    .replace(/\ufeff/g, "") // BOM 제거
    .replace(/\r\n/g, "\n")
    .replace(/\r/g, "\n")
    .trim();

  if (!privateKeyPem.includes("-----BEGIN PRIVATE KEY-----")) {
    throw new Error(
      ".p8 파일 형식이 잘못되었습니다. Apple Developer에서 다시 다운로드한 원본 .p8 파일을 사용하세요.",
    );
  }

  const key = await importPKCS8(privateKeyPem, "ES256");

  const jwt = await new SignJWT({})
    .setProtectedHeader({ alg: "ES256", kid: "KY9B5XN6WQ" })
    .setIssuedAt()
    .setIssuer("5LCBBV4KNB")
    .setSubject("com.touraz.holic.service")
    .setAudience("https://appleid.apple.com")
    .setExpirationTime("180d")
    .sign(key);

  console.log(jwt);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
