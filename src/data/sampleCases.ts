import { SampleCase } from '../types';

export const SAMPLE_CASES: SampleCase[] = [
  {
    id: 'case_normal',
    title: 'Case 1: Normal Bilateral Kidney CT',
    description: 'Axial abdominal CT showing symmetric renal parenchyma with intact corticomedullary margins and no nephrolithiasis or focal lesions.',
    expectedClass: 'NORMAL',
    imagePath: '/samples/sample_scan_normal.jpg',
    patientRef: 'PAT-2026-N104'
  },
  {
    id: 'case_stone',
    title: 'Case 2: Nephrolithiasis (Kidney Stone)',
    description: 'High-attenuation radio-opaque calculus lodged in upper renal calyx with localized acoustic impedance interface.',
    expectedClass: 'KIDNEY_STONE',
    imagePath: '/samples/sample_scan_stone.jpg',
    patientRef: 'PAT-2026-S308'
  },
  {
    id: 'case_cyst',
    title: 'Case 3: Benign Renal Cyst',
    description: 'Well-circumscribed homogeneous fluid-density lesion (-5 to 15 HU) adhering to Bosniak Category I criteria with imperceptible wall.',
    expectedClass: 'KIDNEY_CYST',
    imagePath: '/samples/sample_scan_cyst.jpg',
    patientRef: 'PAT-2026-C512'
  },
  {
    id: 'case_tumor',
    title: 'Case 4: Renal Cell Parenchymal Mass',
    description: 'Heterogeneous enhancing soft-tissue parenchymal distortion with irregular border architecture and high GLCM contrast.',
    expectedClass: 'KIDNEY_TUMOR',
    imagePath: '/samples/sample_scan_tumor.jpg',
    patientRef: 'PAT-2026-T881'
  }
];

export const DISEASE_DETAILS = {
  NORMAL: {
    displayName: 'Normal',
    color: '#10B981',
    badgeClass: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30',
    description: 'Renal parenchyma appears homogeneous with normal corticomedullary differentiation and no focal lesions or nephrolithiasis.',
    radiologicSigns: 'Smooth renal outline, normal parenchyma density (30-40 HU), patent pelvicalyceal system without ectasia or calculi.',
    recommendation: 'Routine age-appropriate renal surveillance. No immediate radiological follow-up required based on current slice.'
  },
  KIDNEY_STONE: {
    displayName: 'Kidney Stone',
    color: '#F59E0B',
    badgeClass: 'bg-amber-500/10 text-amber-400 border-amber-500/30',
    description: 'Hyperdense focal calcification (nephrolithiasis / urolithiasis) identified within the renal calyces, pelvis, or pelviureteric junction.',
    radiologicSigns: 'High CT attenuation (>200-800 HU) with acoustic shadowing on ultrasound; dense white radio-opacity on non-contrast CT.',
    recommendation: 'Evaluate stone dimensions, obstruction/hydronephrosis grade, and referral to clinical urologist for metabolic workup or lithotripsy assessment.'
  },
  KIDNEY_CYST: {
    displayName: 'Kidney Cyst',
    color: '#06B6D4',
    badgeClass: 'bg-cyan-500/10 text-cyan-400 border-cyan-500/30',
    description: 'Well-circumscribed, homogeneous hypodense fluid-filled lesion adhering to Bosniak Category I / II benign cyst criteria.',
    radiologicSigns: 'Near-water attenuation (-10 to +20 HU), imperceptible smooth thin walls, absence of internal septations or contrast enhancement.',
    recommendation: 'Typically benign non-surgical lesion. Periodic serial ultrasound or contrast-enhanced CT monitoring if Bosniak category is ambiguous.'
  },
  KIDNEY_TUMOR: {
    displayName: 'Kidney Tumor',
    color: '#EF4444',
    badgeClass: 'bg-red-500/10 text-red-400 border-red-500/30',
    description: 'Heterogeneous soft-tissue renal parenchymal mass lesion requiring urgent clinical histopathological oncological evaluation.',
    radiologicSigns: 'Deformity of renal contour, irregular soft tissue attenuation (>20 HU), heterogeneous internal architecture, potential necrosis or vascular invasion.',
    recommendation: 'Urgent multiphase renal protocol CT/MRI with contrast; multidisciplinary urologic oncology consultation and surgical biopsy staging.'
  }
};
