package com.gif.eting.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.gif.eting.dto.PhoneDTO;
import com.gif.eting.dto.StampDTO;
import com.gif.eting.dto.StoryDTO;

public interface StoryMapper {
	@Select("SELECT * FROM story_master WHERE story_id = #{story_id}")
	public StoryDTO getStory(@Param("story_id") String storyId);

	/*
	 * 이야기 목록 조회
	 */
	public List<StoryDTO> getStoryList(StoryDTO storyDto);

	/*
	 * 이야기 등록
	 */
	public int insStory(StoryDTO storyDto);

	/*
	 * 이야기 수정
	 */
	public int updStory(StoryDTO storyDto);

	/*
	 * 이야기 삭제
	 */
	public int delStory(StoryDTO storyDto);

	/*
	 * 이야기 대기열에 이야기 등록
	 */
	public int insStoryQueue(StoryDTO storyDto);

	/*
	 * 대기열에서 이야기를 받아온다
	 */
	public StoryDTO getStoryFromQueue(StoryDTO storyDto);

	/*
	 * 대기열에서 받아온 이야기는 대기열에서 삭제
	 */
	public int delStoryFromQueue(StoryDTO storyDto);

	/*
	 * 받은이야기함에 등록
	 */
	public int insRecievedStory(StoryDTO storyDto);

	/*
	 * 받은이야기함에서 불러오기
	 */
	public List<StoryDTO> getStoryFromRecieved(StoryDTO storyDto);

	/*
	 * 사용가능한 스탬프 불러오기
	 */
	public List<StampDTO> getStamp(StampDTO stampDto);

	/*
	 * 스탬프찍기
	 */
	public int insStampToStory(StampDTO stampDto);

	/*
	 * 스탬프찍은 이야기를 받은이야기함에서 지우기
	 */
	public int delStoryFromRecieved(StampDTO stampDto);
	
	/*
	 * 이야기ID를 갖고 스탬프 불러오기
	 */
	public List<StampDTO> getStampByStory(String storyId);
	
	/*
	 * 이야기ID를 갖고 스탬프 불러오기
	 */
	public List<String> getStampedStoryByPhoneId(String storyId);
	
	/*
	 * 핸드폰 고유번호 저장
	 */
	public int insPhoneRegistration(PhoneDTO phoneDto);
	
	/*
	 * 핸드폰 고유번호 불러오기
	 * @param story_id
	 */
	public PhoneDTO getPhoneRegistration(String story_id);	

	/*
	 * 신고등록
	 */
	public int insStoryReport(StoryDTO storyDto);
	
	/*
	 * 폰 전체 목록 가져오기
	 */
	public List<PhoneDTO> getPhoneRegistrationList();

	/*
	 * 무작위 이야기를 불러온다
	 */
	public StoryDTO getRandomStory(String phoneId);
	
	/*
	 * 무작위로 이용자 한명을 선택한다.
	 */
	public PhoneDTO getRandomPhone();
	
	/*
	 * 폰 전체 개수
	 */
	public Integer getPhoneRegistrationCnt();
	
}
