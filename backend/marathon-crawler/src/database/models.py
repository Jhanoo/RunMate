"""
데이터베이스 모델 정의
"""
from sqlalchemy import Column, String, DateTime, ForeignKey, text
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import relationship

Base = declarative_base()

class Marathon(Base):
    """마라톤 이벤트 모델"""
    __tablename__ = 'marathons'
    
    marathon_id = Column(UUID(as_uuid=True), primary_key=True, server_default=text("gen_random_uuid()"))
    name = Column(String(255), nullable=False)
    date = Column(DateTime(timezone=True), nullable=False)
    location = Column(String(300), nullable=False)
    created_at = Column(DateTime(timezone=True), nullable=False, server_default=text("now()"))
    
    # 관계 설정
    distances = relationship("MarathonDistance", back_populates="marathon", cascade="all, delete-orphan")
    
    def __repr__(self):
        return f"<Marathon(name='{self.name}', date='{self.date}', location='{self.location}')>"


class MarathonDistance(Base):
    """마라톤 거리 모델"""
    __tablename__ = 'marathon_distances'
    
    distance_id = Column(UUID(as_uuid=True), primary_key=True, server_default=text("gen_random_uuid()"))
    marathon_id = Column(UUID(as_uuid=True), ForeignKey('marathons.marathon_id'), nullable=False)
    distance = Column(String(30), nullable=False)
    
    # 관계 설정
    marathon = relationship("Marathon", back_populates="distances")
    
    def __repr__(self):
        return f"<MarathonDistance(distance='{self.distance}')>"