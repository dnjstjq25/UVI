# 대학교 캡스톤 프로젝트(졸업작품)
## 내 위치 UVI

### 개요
1. 프로젝트 개요
 - 스마트폰의 조도 센서를 통해 태양광의 조도를 측정 후 사용자의 위치에서의 UVI를 제공하는 딥러닝 기반의 자외선 정보 산출 어플리케이션
2. 프로젝트 특징
 1. 전문 광계측장비를 통해 실측한 자연광 데이터를 분석하여 딥러닝을 위한 데이터 셋을 도출
 2. 딥러닝 서버에서 DNN 모델의 구축 및 학습을 수행한 후 TensorFlow Lite Converter를 통해 모바일용 딥러닝 모델로 변환 후 이를 딥러닝 서버를 통해 사용자의 스마트폰으로 배포
 3. 스마트폰의 조도 센싱 값과 GPS 센서 기반의 위치 정보를 함께 입력하여 자외선(UVI) 정보를 산출
 4. 산출된 자외선 정보를 바탕으로 UVI 단계 설정 및 단계별 주의사항 출력

### 계획
1. 인력
 - 팀장1 : 기초 데이터 분석, 딥러닝 모델 개발, 서버 구축 및 관리
 - 팀원1 : 어플리케이션 내 센서 연동, 시스템 프로세스 구축
 - 팀원2 : 어플리케이션 GUI 제작, GitHub를 이용한 프로젝트 관리
2. 개발 절차
 - 폭포수 모델

### 개발 환경
1. 서버
 - OS: Ubuntu Linux 16.04 LTS
 - 개발 언어 : Python
 - 이용 SW : MySQL, Hadoop, Keras, Tensorflow, Apache Zeppelin, Spark
2. 어플리케이션
 - OS : Windows 10 Home
 - 개발 언어 : Java
 - 이용 SW : Android Studio, Tensorflow Lite