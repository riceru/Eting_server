package com.gif.eting.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.View;

import com.gif.eting.dao.StoryMapper;
import com.gif.eting.dto.StampDTO;
import com.gif.eting.dto.StoryDTO;

@Controller
public class MainController {
	Logger log = LoggerFactory.getLogger(MainController.class);

	@Autowired
	@Qualifier(value = "jsonView")
	private View jsonView;

	@Autowired
	StoryMapper storyMapper;

	/*
	 * 테스트
	 */
	@RequestMapping(value = "/test")
	public View test(Model model) {
		// 답사 기본정보 목록조회
		model.addAttribute("return", "123");
		return jsonView;
	}

	// 처음페이지 - 암호해제
	@RequestMapping(value = "/")
	public String intro() {

		return "intro";
	}

	// 메인페이지
	@RequestMapping(value = "/main")
	public String main(HttpServletRequest request) {
		String phoneId = request.getParameter("phoneId");
		log.debug("phoneId = " + phoneId);
		HttpSession session = request.getSession(true);
		session.setAttribute("phoneId", phoneId); // 세션에 phoneId 저장

		StoryDTO et = new StoryDTO();
		et.setPhone_id(phoneId);
		List<StoryDTO> list = storyMapper.getStoryFromRecieved(et);
		request.setAttribute("list", list);

		List<StampDTO> stampList = storyMapper.getStamp(null);
		request.setAttribute("stampList", stampList);

		return "main";
	}

	// 이야기 입력 화면
	@RequestMapping(value = "/write")
	public String input() {

		return "write";
	}

	// 이야기 저장하기
	// 1. 이야기를 저장한 후 (story_master),
	// 2. 보내질 이야기 대기열에 넣고 (story_queue),
	// 3. 대기열에서 이야기를 가져온다 (story_queue),
	// 4. 가져온 이야기는 대기열에서 삭제한다. (story_queue),
	// 5. 가져온 이야기를 자신의 받은이야기함에 넣는다.(recieved_story)
	@RequestMapping(value = "/saveStory")
	public View saveStory(@ModelAttribute StoryDTO et, Model model) {
		String phoneId = et.getPhone_id(); // 입력한사람 기기 고유값

		storyMapper.insStory(et); // 1. 이야기저장
		StoryDTO myStory = storyMapper.getStory(et.getStory_id());	//저장한 정보를 불러와서
		model.addAttribute("myStory",myStory);	//사용자에게 다시 전달
		
		storyMapper.insStoryQueue(et); // 2. 이야기대기열에 입력한 이야기 저장
		
		StoryDTO recievedStory = storyMapper.getStoryFromQueue(et); // 3. 대기열에서 이야기를 가져온다

		// 받아온게 있으면
		if (recievedStory != null) {
			storyMapper.delStoryFromQueue(recievedStory); // 4. 가져온 이야기는 대기열에서 삭제한다.
			recievedStory.setPhone_id(phoneId); // phoneId를 입력한사람의 것으로 바꿈.
			storyMapper.insRecievedStory(recievedStory); // 5.대기열에서 가져온 이야기가 있으면 받은이야기함에 넣는다.
			// 대기열에서 가져온 이야기가 있으면 json으로 입력한사람 기기로 리턴해준다
			model.addAttribute("recievedStory", recievedStory);
		} else {
			// 없으면 뭐행?
			// 대기열에서 가져온 이야기가 없으면 아무것도 없음 :p
			model.addAttribute("recievedStory", null);
		}

		return jsonView;
	}

	// 이야기 목록 화면
	@RequestMapping(value = "/list")
	public String list(HttpServletRequest request) {
		HttpSession session = request.getSession(true);
		String phoneId = (String) session.getAttribute("phoneId");
		StoryDTO et = new StoryDTO();
		et.setPhone_id(phoneId);

		// 자기 이야기 목록 받아오기
		setStoryList(request, et);
		return "list";
	}

	// 자기이야기 목록
	public void setStoryList(HttpServletRequest request, StoryDTO et) {
		List<StoryDTO> list = storyMapper.getStoryList(et);
		request.setAttribute("list", list);
	}

	// 스탬프찍기
	// 스탬프 찍고난 후 해당 스토리를 받은이야기함에서 지우기
	@RequestMapping(value = "/saveStamp")
	public View saveStamp(StampDTO et) {

		storyMapper.insStampToStory(et); // 스탬프찍기
		storyMapper.delStoryFromRecieved(et); // 스탬프찍은 이야기를 받음이야기함에서 지우기

		return jsonView;
	}

	// 스탬프 정보 폰에 전송
	@RequestMapping(value = "/getStamp")
	public View getStamp(HttpServletRequest request, Model model) {
		String storyId = request.getParameter("storyId");

		List<StampDTO> stampList = storyMapper.getStampByStory(storyId); // 스탬프찍기
		model.addAttribute("stampList", stampList);

		return jsonView;
	}
	
	/**
	 * 폰에서 스탬프 최고 번호를 수신하면
	 * 서버에서 최고번호보다 최신 목록을 받아서
	 * 응답으로 스탬프 목록을 내려준다 
	 */
	@RequestMapping(value = "/checkStamp")
	public View checkStamp(HttpServletRequest request, Model model){
		String stampId = request.getParameter("stampId");	//폰에서 받아온 스탬프아이디 최대값
		
		StampDTO stampDto = new StampDTO();
		stampDto.setStamp_id(stampId);
		List<StampDTO> stampList = storyMapper.getStamp(stampDto); // 폰보다 최신인 스탬프목록을 받아옴
		model.addAttribute("stampList", stampList);	//모델에 박아서 넘김
		
		return jsonView;
	}

}