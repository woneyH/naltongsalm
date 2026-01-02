import matplotlib.pyplot as plt
import pandas as pd
import io
import os
from azure.storage.blob import BlobServiceClient

def generate_weather_graph(forecast_list):
    # 1. 서버 환경 설정 (GUI 창 안 뜨게 설정)
    plt.switch_backend('Agg')
    
    # 2. 리스트를 DataFrame으로 변환
    # (processor.py에서 넘겨준 리스트: time, temp, laundryScore, drynessScore 포함)
    df = pd.DataFrame(forecast_list)
    
    # 3. 그래프 그리기 설정
    fig, ax1 = plt.subplots(figsize=(10, 6))
    
    # X축: 시간
    times = df['time']
    
    # [왼쪽 Y축] 빨래 지수 (막대 그래프)
    color_laundry = '#ff9f43'
    ax1.set_xlabel('Time')
    ax1.set_ylabel('Laundry Score', color=color_laundry, fontweight='bold')
    ax1.bar(times, df['laundryScore'], color=color_laundry, alpha=0.6, label='Laundry Score')
    ax1.tick_params(axis='y', labelcolor=color_laundry)
    ax1.set_ylim(0, 100)

    # [오른쪽 Y축] 기온 (선 그래프)
    ax2 = ax1.twinx()
    color_temp = '#2d82ff'
    ax2.set_ylabel('Temperature (°C)', color=color_temp, fontweight='bold')
    ax2.plot(times, df['temp'], color=color_temp, marker='o', linewidth=2, label='Temp')
    ax2.tick_params(axis='y', labelcolor=color_temp)

    # 제목 및 그리드 설정
    plt.title("24-Hour Laundry & Weather Forecast", fontsize=14, pad=20)
    ax1.grid(True, axis='y', linestyle='--', alpha=0.5)
    
    # 레이아웃 조정 (글자가 짤리지 않게)
    plt.tight_layout()

    # 4. 이미지를 메모리에 저장
    buf = io.BytesIO()
    plt.savefig(buf, format='png', dpi=100)
    buf.seek(0)
    plt.close(fig)

    # 5. Azure Blob Storage에 업로드
    try:
        connect_str = os.environ.get('AzureWebJobsStorage')
        if not connect_str:
            print("스토리지 연결 문자열(AzureWebJobsStorage)이 없습니다.")
            return

        blob_service_client = BlobServiceClient.from_connection_string(connect_str)
        # 컨테이너 이름 'graphs', 파일명 'weather_detail.png'
        blob_client = blob_service_client.get_blob_client(container="graphs", blob="weather_detail.png")

        blob_client.upload_blob(buf.read(), overwrite=True)
        print("그래프 업로드 성공")
    except Exception as e:
        print(f"그래프 업로드 실패: {e}")