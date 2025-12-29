import matplotlib.pyplot as plt
import io
import os
from azure.storage.blob import BlobServiceClient

def generate_weather_graph(df):
    plt.switch_backend('Agg') # 서버용 설정
    
    fig, ax = plt.subplots(figsize=(10, 5))
    df = df.sort_values('baseTime')
    times = df['baseTime'].apply(lambda x: f"{x[:2]}:{x[2:]}")

    ax.plot(times, df['REH'], marker='o', label='Humidity (%)', color='#3498db')
    if 'laundry_score' in df.columns:
        ax.plot(times, df['laundry_score'], marker='s', label='Laundry Index', color='#e67e22')

    ax.set_title("Weather Analysis Chart")
    ax.set_ylim(0, 110)
    ax.legend()
    ax.grid(True, linestyle='--', alpha=0.5)

    # 1. 이미지를 메모리에 저장
    buf = io.BytesIO()
    plt.savefig(buf, format='png')
    buf.seek(0)
    plt.close(fig)

    # 2. Azure Blob Storage에 업로드
    try:
        # Azure 함수 앱의 연결 문자열을 사용합니다 (AzureWebJobsStorage 환경변수 활용)
        connect_str = os.environ.get('AzureWebJobsStorage')
        blob_service_client = BlobServiceClient.from_connection_string(connect_str)
        blob_client = blob_service_client.get_blob_client(container="graphs", blob="weather_detail.png")

        # 기존 파일이 있으면 덮어쓰기
        blob_client.upload_blob(buf.read(), overwrite=True)
        return "Upload Success"
    except Exception as e:
        return f"Upload Failed: {e}"