import React, { createContext, useContext, useState, useEffect } from 'react';
import { DiagnosticReport } from '../types';

interface DiagnosticContextType {
  reports: DiagnosticReport[];
  currentReport: DiagnosticReport | null;
  setCurrentReport: (r: DiagnosticReport | null) => void;
  saveReport: (r: DiagnosticReport) => void;
  deleteReport: (id: string) => void;
  clearAllReports: () => void;
}

const DiagnosticContext = createContext<DiagnosticContextType | undefined>(undefined);

export const DiagnosticProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [reports, setReports] = useState<DiagnosticReport[]>(() => {
    const saved = localStorage.getItem('kidney_ai_reports');
    return saved ? JSON.parse(saved) : [];
  });

  const [currentReport, setCurrentReport] = useState<DiagnosticReport | null>(null);

  useEffect(() => {
    localStorage.setItem('kidney_ai_reports', JSON.stringify(reports));
  }, [reports]);

  const saveReport = (report: DiagnosticReport) => {
    setReports(prev => [report, ...prev.filter(r => r.id !== report.id)]);
    setCurrentReport(report);
  };

  const deleteReport = (id: string) => {
    setReports(prev => prev.filter(r => r.id !== id));
    if (currentReport?.id === id) {
      setCurrentReport(null);
    }
  };

  const clearAllReports = () => {
    setReports([]);
    setCurrentReport(null);
  };

  return (
    <DiagnosticContext.Provider
      value={{
        reports,
        currentReport,
        setCurrentReport,
        saveReport,
        deleteReport,
        clearAllReports
      }}
    >
      {children}
    </DiagnosticContext.Provider>
  );
};

export const useDiagnostic = () => {
  const context = useContext(DiagnosticContext);
  if (!context) throw new Error('useDiagnostic must be used within a DiagnosticProvider');
  return context;
};
