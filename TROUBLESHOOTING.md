## 🛠️ Spring AI 자동화 구조의 한계 및 RestTemplate 전환 (2026-06-05)

### 1. 문제 상황
* 'OpenAiChatModel' 라이브러리를 사용하여 API를 호출할 때, OpenAI 서버로부터 'Unrecognized request argument supplied: extra_body' 오류(HTTP 400)가 발생하며 통신이 거부됨.

### 2. 원인 분석
* **프레임워크 내부 버그**: 'OpenAiChatModel'은 다양한 옵션을 자동으로 조립하여 요청을 보냄. 이 과정에서 라이브러리 내부 엔진의 버전 호환성 문제로 인해, 최신 OpenAI 규격에 맞지 않는 유령 파라미터('extra_body')가 강제로 포함되어 요청이 날아간 것이 원인이었음.
* **의존성 충돌**: AI 스타터 라이브러리가 내포한 수많은 서드파티 의존성들이 스프링 부트 버전과 충돌을 일으켜 에디터의 자바 빌드 서버까지 마비됨.

### 3. 해결 방법
* **외부 라이브러리 제거**: 호환성 문제를 일으키는 'spring-ai' 의존성을 제거함.
* **표준 기술 기반의 서비스 레이어 구현**: 스프링 부트 내장 HTTP 클라이언트인 'RestTemplate'을 활용하여, OpenAI 공식 문서의 필수 규격('model', 'messages')만 순수하게 조립해 통신하는 'OpenAiService'를 직접 구현함.
* **결과**: 프레임워크 종속성을 탈출하여 안정적인 **Controller - Service (3-Tier) 아키텍처**를 완성하고 정상 응답을 수신함.