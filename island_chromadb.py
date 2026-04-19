#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
전남 섬 청크를 ChromaDB 벡터 DB에 저장하는 스크립트
"""

import json
from pathlib import Path

import chromadb
from chromadb.utils import embedding_functions


def load_chunks(chunks_path: Path) -> list[dict]:
    """청크 JSON 로드"""
    with open(chunks_path, "r", encoding="utf-8") as f:
        return json.load(f)


def prepare_for_chromadb(chunks: list[dict]) -> tuple[list[str], list[dict], list[str]]:
    """
    ChromaDB에 넣기 위한 형식으로 변환
    - documents: 임베딩할 텍스트 리스트
    - metadatas: 메타데이터 (str, int, float, bool만 허용)
    - ids: 고유 ID 리스트
    """
    documents = []
    metadatas = []
    ids = []

    for chunk in chunks:
        documents.append(chunk["text"])
        ids.append(f"island_{chunk['chunk_id']}")

        meta = chunk["metadata"]
        chroma_meta = {
            "island_name": meta.get("island_name", ""),
            "district": meta.get("district", ""),
            "area_km2": meta.get("area_km2", ""),
            "population": meta.get("population", ""),
            "specialty": meta.get("specialty", ""),
        }
        metadatas.append(chroma_meta)

    return documents, metadatas, ids


def main():
    base_dir = Path(__file__).parent
    chunks_path = base_dir / "island_chunks" / "island_chunks.json"
    chroma_persist_dir = base_dir / "chroma_island_db"

    if not chunks_path.exists():
        print(f"청크 파일을 찾을 수 없습니다: {chunks_path}")
        print("먼저 parse_island_csv.py를 실행해주세요.")
        return

    print("청크 로드 중...")
    chunks = load_chunks(chunks_path)
    documents, metadatas, ids = prepare_for_chromadb(chunks)

    # 다국어 한국어 지원 임베딩 모델 사용
    print("임베딩 모델 로드 중 (paraphrase-multilingual-MiniLM-L12-v2)...")
    ef = embedding_functions.SentenceTransformerEmbeddingFunction(
        model_name="paraphrase-multilingual-MiniLM-L12-v2"
    )

    # ChromaDB 클라이언트 (로컬 영속화)
    print("ChromaDB 초기화 중...")
    client = chromadb.PersistentClient(path=str(chroma_persist_dir))

    # 컬렉션 생성 또는 재사용 (기존 데이터 덮어쓰기)
    collection_name = "jeonnam_islands"
    try:
        client.delete_collection(collection_name)
    except Exception:
        pass

    collection = client.create_collection(
        name=collection_name,
        embedding_function=ef,
        metadata={"description": "전라남도 가고 싶은 섬 정보"},
    )

    # 청크 추가 (배치로)
    print(f"벡터 DB에 {len(documents)}개 청크 저장 중...")
    collection.add(
        documents=documents,
        metadatas=metadatas,
        ids=ids,
    )

    print(f"\n저장 완료: {chroma_persist_dir}")
    print(f"컬렉션: {collection_name}")

    # 검색 테스트
    print("\n[검색 테스트] '낭도 특산물'로 쿼리:")
    results = collection.query(
        query_texts=["낭도 특산물이 뭐예요?"],
        n_results=3,
    )
    for i, (doc, meta) in enumerate(
        zip(results["documents"][0], results["metadatas"][0])
    ):
        print(f"  {i + 1}. {meta['island_name']} ({meta['district']})")
        print(f"     {doc[:80]}...")


if __name__ == "__main__":
    main()
