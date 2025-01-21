# DevOnOff
스터디 모집, 채팅, 화상채팅을 하나의 플랫폼에서 진행할 수 있는 서비스입니다.

## 프로젝트 기능 및 설계


* 회원가입 기능
  * 사용자는 회원가입을 할 수 있다.
  * 회원가입시 아이디,패스워드를 입력받으며, 아이디는 unique 해야한다.
* 로그인 기능
  * 사용자는 로그인을 할 수 있다. 로스인시 회원가입때 사용한 아이디와 패스워드가 일치해야 한다.
* 게시글 작성 기능
  * 로그인한 사용자는 게시글 제목(텍스트), 게시글 썸네일(이미지), 내용(텍스트)를 작성할 수 있다.
* 게시글 목록 조회 기능
  * 사용자는 게시글 목록를 확인할 수 있다.
* 게시글 세부사항 조회 기능
  * 사용자는 게시글 목록를 확인할 수 있다.
* 댓글 작성 기능
  * 로그인한 사용자는 게시글에 댓글(텍스트)을 작성할 수 있다.
* 댓글 목록 조회 기능
  * 사용자는 게시글의 댓글을 조회할 수 있다.
* 채팅
  * 스터디가 생성되면 스터디내 인원들과 채팅을 할 수 있다.
* 화상 채팅
  * 스터디가 생성되면 스터디내 인원들과 화상채팅을 할 수 있다.
* 학습 시간 기록
  * 스터디내 모든 인원이 화상채팅에 참여할 경우 학습 시간이 기록된다.
  * 기록된 학습시간이 많은 기준 상위 10개의 스터디를 조회할 수 있다.
* 알림
  * 사용자의 게시글에 댓글이 달리는 경우 알림을 받을 수 있다.
  * 사용자의 댓글에 대댓글이 달리는 경우 알림을 받을 수 있다.
  * 사용자의 스터디 모집글에 스터디 신청이 온 경우 알림을 받을 수 있다.
  * 사용자의 스터디 신청이 수락된 경우 알림을 받을 수 있다.
  * 사용자의 스터디 신청이 거절된 경우 알림을 받을 수 있다.
  * 사용자가 신청한 스터디가 생성된 경우 알림을 받을 수 있다.

## 시연영상
[![Video Label](http://img.youtube.com/vi/stJ2JhYMtrs/0.jpg)](https://youtu.be/stJ2JhYMtrs)

## ERD
![DevOnOff (2)](https://github.com/user-attachments/assets/4a7ed5a4-e4ee-433a-9c23-28aa7a3c5aaf)

## 기술스택
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white) 
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Redis](https://img.shields.io/badge/redis-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white)![MySQL](https://img.shields.io/badge/mysql-4479A1.svg?style=for-the-badge&logo=mysql&logoColor=white)
<img src="https://img.shields.io/badge/Amazon%20EC2-FF9900?style=for-the-badge&logo=Amazon%20EC2&logoColor=white">
<img src="https://img.shields.io/badge/Amazon%20S3-569A31?style=for-the-badge&logo=Amazon%20S3&logoColor=white">
<img src="https://img.shields.io/badge/webRTC-333333?style=for-the-badge&logo=webRTC&logoColor=white">
<img src="https://img.shields.io/badge/webSocket-333333?style=for-the-badge&logo=webSocket&logoColor=white">
