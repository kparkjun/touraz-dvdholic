#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
전라남도 가고 싶은 섬 CSV 파싱 및 RAG용 청킹 스크립트
"""

import csv
import json
from pathlib import Path


def parse_csv(file_path: str) -> list[dict]:
    """CSV 파일 파싱 (인코딩 처리 포함)"""
    rows = []
    encodings = ["utf-8", "utf-8-sig", "cp949", "euc-kr"]

    for encoding in encodings:
        try:
            with open(file_path, "r", encoding=encoding) as f:
                reader = csv.DictReader(f)
                for row in reader:
                    # 빈 행 스킵
                    if not any(row.values()):
                        continue
                    rows.append(dict(row))
            return rows
        except UnicodeDecodeError:
            continue
    raise ValueError(f"파일 인코딩을 확인할 수 없습니다: {file_path}")


def format_value(val: str) -> str:
    """값 정규화 (X, x 등 빈값 표기 처리)"""
    if not val or str(val).strip() in ("X", "x", ""):
        return ""
    return str(val).strip()


def island_to_chunk(row: dict, index: int) -> dict:
    """
    각 섬 데이터를 RAG 검색에 적합한 텍스트 청크로 변환
    - text: 임베딩/검색용 본문
    - metadata: 필터링·출력용 메타데이터
    """
    name = format_value(row.get("섬 이름", ""))
    district = format_value(row.get("행정구역", ""))

    # 핵심 정보 추출
    area = format_value(row.get("총 면적(제곱킬로미터)", ""))
    coastline = format_value(row.get("해안선 길이(킬로미터)", ""))
    distance = format_value(row.get("육지와의 거리(킬로미터_직선)", ""))
    households = format_value(row.get("가구", ""))
    population = format_value(row.get("인구", ""))
    water = format_value(row.get("식수(상수도)", ""))
    electricity = format_value(row.get("전기", ""))
    specialty = format_value(row.get("특산물", ""))
    transport_info = format_value(row.get("요금(왕복_편도)_소요시간", ""))
    departure = format_value(row.get("출항지_기항지", ""))
    island_transport = format_value(row.get("섬내 교통수단", ""))
    facilities = format_value(row.get("읍면동사무소", ""))
    police = format_value(row.get("경찰관서", ""))
    fire = format_value(row.get("소방관서", ""))
    finance = format_value(row.get("금융기관", ""))
    school = format_value(row.get("초중고 운영 및 폐교 현황", ""))
    medical = format_value(row.get("의료", ""))
    bridge = format_value(row.get("연도_연륙(차도선여부)", ""))

    # RAG 검색에 최적화된 자연어 텍스트 구성
    text_parts = [
        f"{name}은(는) 전라남도 {district}에 위치한 섬입니다.",
        f"면적은 {area}㎢, 해안선 길이 {coastline}km입니다.",
    ]
    if distance:
        text_parts.append(f"육지와의 거리는 {distance}입니다.")
    if population:
        text_parts.append(f"인구 {population}명, 가구 {households}가구가 거주합니다.")
    if water:
        text_parts.append(f"식수는 {water}를 사용합니다.")
    if electricity:
        text_parts.append(f"전기는 {electricity}를 이용합니다.")

    if specialty:
        text_parts.append(f"특산물은 {specialty}입니다.")

    # 시설 정보
    facility_list = []
    if facilities:
        facility_list.append(f"행정기관 {facilities}")
    if police:
        facility_list.append(f"경찰 {police}")
    if fire:
        facility_list.append(f"소방 {fire}")
    if finance:
        facility_list.append(f"금융기관 {finance}")
    if school:
        facility_list.append(f"학교 {school}")
    if medical:
        facility_list.append(f"의료 {medical}")
    if facility_list:
        text_parts.append("시설: " + ", ".join(facility_list) + ".")

    # 교통 정보
    if bridge:
        text_parts.append(f"연륙/연도: {bridge}.")
    if transport_info:
        text_parts.append(f"배편: {transport_info}")
    if departure:
        text_parts.append(f"출항지-기항지: {departure}")
    if island_transport:
        text_parts.append(f"섬 내 교통: {island_transport}")

    text = " ".join(text_parts)

    return {
        "chunk_id": index,
        "text": text,
        "metadata": {
            "island_name": name,
            "district": district,
            "area_km2": area,
            "population": population,
            "specialty": specialty,
            "raw": {k: format_value(v) for k, v in row.items() if format_value(v)},
        },
    }


def main():
    csv_path = Path(r"c:\Users\USER\Downloads\전라남도_가고 싶은 섬_20250718.csv")
    output_dir = Path(__file__).parent / "island_chunks"

    if not csv_path.exists():
        print(f"파일을 찾을 수 없습니다: {csv_path}")
        return

    print("CSV 파싱 중...")
    rows = parse_csv(str(csv_path))
    print(f"총 {len(rows)}개 섬 데이터 파싱 완료")

    print("청킹 중...")
    chunks = [island_to_chunk(row, i + 1) for i, row in enumerate(rows)]

    output_dir.mkdir(exist_ok=True)

    # JSON 출력 (RAG 파이프라인용)
    output_json = output_dir / "island_chunks.json"
    with open(output_json, "w", encoding="utf-8") as f:
        json.dump(chunks, f, ensure_ascii=False, indent=2)
    print(f"청크 JSON 저장: {output_json}")

    # 텍스트만 추출 (임베딩용)
    output_txt = output_dir / "island_chunks_text.txt"
    with open(output_txt, "w", encoding="utf-8") as f:
        for c in chunks:
            f.write(f"--- Chunk {c['chunk_id']} ({c['metadata']['island_name']}) ---\n")
            f.write(c["text"] + "\n\n")
    print(f"텍스트 저장: {output_txt}")

    print("\n[샘플 청크 1개]")
    print(json.dumps(chunks[0], ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
