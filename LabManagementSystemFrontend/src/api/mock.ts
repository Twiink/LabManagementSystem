export const mockLabs = [
  { id: 1, name: "化学实验室 A", location: "1号楼 203", capacity: 40, status: "IDLE" },
  { id: 2, name: "物理实验室 B", location: "2号楼 101", capacity: 30, status: "IN_USE" },
  { id: 3, name: "计算机机房 C", location: "3号楼 405", capacity: 60, status: "RESERVED" },
  { id: 4, name: "生物实验室 D", location: "1号楼 205", capacity: 35, status: "MAINTENANCE" },
];

export const mockDevices = [
  { id: 1, labId: 1, name: "光谱仪", model: "SP-2025", status: "IDLE" },
  { id: 2, labId: 1, name: "离心机", model: "CF-500", status: "IN_USE" },
  { id: 3, labId: 2, name: "示波器", model: "OS-100", status: "IDLE" },
  { id: 4, labId: 3, name: "高性能服务器", model: "DGX-A100", status: "RESERVED" },
];

export const mockReservations = [
  { id: 101, title: "分析化学", startTime: "2025-01-15T08:30:00Z", endTime: "2025-01-15T10:30:00Z", status: "APPROVED", type: "COURSE" },
  { id: 102, title: "个人实验", startTime: "2025-01-16T14:00:00Z", endTime: "2025-01-16T16:00:00Z", status: "PENDING", type: "SINGLE" },
];

export const mockStats = {
  totalLabs: 12,
  totalDevices: 48,
  activeReservations: 5,
  pendingApprovals: 3
};
