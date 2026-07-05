import { DEFAULT_TAB_KEY, type TabKey } from '@/constants/tabs';
import { create } from 'zustand';

type EntryMode = 'continue' | 'skip';

type CaseState = {
  caseId: string;
  entryMode: EntryMode;
  activeTab: TabKey;
  refreshSeed: number;
  initializeWorkspace: (params: { caseId?: string | null; mode?: EntryMode }) => void;
  setCaseId: (caseId: string) => void;
  setActiveTab: (tabKey: TabKey) => void;
  bumpRefreshSeed: () => void;
  reset: () => void;
};

const initialState = {
  caseId: '',
  entryMode: 'skip' as EntryMode,
  activeTab: DEFAULT_TAB_KEY,
  refreshSeed: 0,
};

export const useCaseStore = create<CaseState>((set) => ({
  ...initialState,
  initializeWorkspace: ({ caseId, mode = 'skip' }: { caseId?: string | null; mode?: EntryMode }) =>
    set(() => ({
      caseId: caseId ?? '',
      entryMode: mode,
      activeTab: DEFAULT_TAB_KEY,
      refreshSeed: caseId ? Date.now() : 0,
    })),
  setCaseId: (caseId: string) =>
    set((state: CaseState) => ({
      caseId,
      entryMode: caseId ? 'continue' : state.entryMode,
    })),
  setActiveTab: (activeTab: TabKey) => set(() => ({ activeTab })),
  bumpRefreshSeed: () => set(() => ({ refreshSeed: Date.now() })),
  reset: () => set(() => ({ ...initialState })),
}));

export function resetCaseStore() {
  useCaseStore.getState().reset();
}
