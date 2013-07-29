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

import com.gif.eting.dao.EtingMapper;
import com.gif.eting.vo.EtingVO;
import com.gif.eting.vo.StampVO;

@Controller
public class MainController {
	Logger log = LoggerFactory.getLogger(MainController.class);

	@Autowired
	@Qualifier(value = "jsonView")
	private View jsonView;

	@Autowired
	EtingMapper etingMapper;

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

		EtingVO et = new EtingVO();
		et.setPhone_id(phoneId);
		List<EtingVO> list = etingMapper.getEtingFromRecieved(et);
		request.setAttribute("list", list);

		List<StampVO> stampList = etingMapper.getStamp();
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
	@RequestMapping(value = "/insert")
	public View insert(@ModelAttribute EtingVO et, Model model) {
		String phoneId = et.getPhone_id(); // 입력한사람 기기 고유값

		etingMapper.insEting(et); // 1. 이야기저장
		etingMapper.insEtingQueue(et); // 2. 이야기대기열에 저장

		et = etingMapper.getEtingFromQueue(et); // 3. 대기열에서 이야기를 가져온다

		// 받아온게 있으면
		if (et != null) {
			etingMapper.delEtingFromQueue(et); // 4. 가져온 이야기는 대기열에서 삭제한다.
			et.setPhone_id(phoneId); // phoneId를 입력한사람의 것으로 바꿈.
			etingMapper.insRecievedEting(et); // 5.대기열에서 가져온 이야기가 있으면 받은이야기함에 넣는다.
			// 대기열에서 가져온 이야기가 있으면 json으로 입력한사람 기기로 리턴해준다
			model.addAttribute("story", et);
		} else {
			// 없으면 뭐행?
			et = new EtingVO();
			et.setPhone_id(phoneId);
			// 대기열에서 가져온 이야기가 없으면 아무것도 없음 :p
			model.addAttribute("story", null);
		}

		return jsonView;
	}

	// 이야기 목록 화면
	@RequestMapping(value = "/list")
	public String list(HttpServletRequest request) {
		HttpSession session = request.getSession(true);
		String phoneId = (String) session.getAttribute("phoneId");
		EtingVO et = new EtingVO();
		et.setPhone_id(phoneId);

		// 자기 이야기 목록 받아오기
		setEtingList(request, et);
		return "list";
	}

	// 자기이야기 목록
	public void setEtingList(HttpServletRequest request, EtingVO et) {
		List<EtingVO> list = etingMapper.getEtingList(et);
		request.setAttribute("list", list);
	}

	// 스탬프찍기
	// 스탬프 찍고난 후 해당 스토리를 받은이야기함에서 지우기
	@RequestMapping(value = "/stamp")
	public String stamp(HttpServletRequest request) {
		String storyId = request.getParameter("storyId");
		String stampId = request.getParameter("stampId");

		StampVO et = new StampVO();
		et.setStamp_id(stampId);
		et.setStory_id(storyId);

		etingMapper.insStampToEting(et); // 스탬프찍기
		etingMapper.delEtingFromRecieved(et); // 스탬프찍은 이야기를 받음이야기함에서 지우기

		return this.main(request);
	}

}
