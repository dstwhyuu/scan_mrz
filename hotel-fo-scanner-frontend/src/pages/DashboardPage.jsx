import React, { useRef, useState, useCallback } from 'react';
import Webcam from 'react-webcam';
import { Camera, RefreshCw, Upload, FileText, CheckCircle2, AlertCircle } from 'lucide-react';
import { toast } from 'sonner';
import apiClient from '../services/apiClient';

const DashboardPage = () => {
  const webcamRef = useRef(null);
  const [isCapturing, setIsCapturing] = useState(false);
  const [capturedImage, setCapturedImage] = useState(null);
  const [isProcessing, setIsProcessing] = useState(false);
  const [scanResult, setScanResult] = useState(null);

  const capture = useCallback(() => {
    const imageSrc = webcamRef.current.getScreenshot();
    setCapturedImage(imageSrc);
    setScanResult(null); // Reset prev result
  }, [webcamRef]);

  const retake = () => {
    setCapturedImage(null);
    setScanResult(null);
  };

  // Convert Base64 to File object to send as multipart/form-data
  const dataURLtoFile = (dataurl, filename) => {
    let arr = dataurl.split(','),
        mime = arr[0].match(/:(.*?);/)[1],
        bstr = atob(arr[1]), 
        n = bstr.length, 
        u8arr = new Uint8Array(n);
        
    while(n--){
        u8arr[n] = bstr.charCodeAt(n);
    }
    return new File([u8arr], filename, {type:mime});
  };

  const uploadAndScan = async () => {
    if (!capturedImage) return;

    try {
      setIsProcessing(true);
      const file = dataURLtoFile(capturedImage, 'passport.jpg');
      
      const formData = new FormData();
      formData.append('passportImage', file);

      // Endpoint sesuai dengan arsitektur (ScanController)
      // Kita harus meng-override Content-Type default (application/json)
      // menjadi multipart/form-data agar axios mengurus boundary-nya secara otomatis
      const response = await apiClient.post('/scans', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });

      setScanResult(response.data);
      toast.success('Pemindaian berhasil!');
    } catch (error) {
      console.error(error);
      const errData = error.response?.data;
      if (errData && errData.requiresManualReview) {
        setScanResult(errData);
        toast.warning('Pemindaian kurang jelas, mohon tinjau secara manual.');
      } else {
        toast.error('Gagal melakukan pemindaian ke server.');
      }
    } finally {
      setIsProcessing(false);
    }
  };

  const handleFileUpload = (e) => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        setCapturedImage(reader.result);
        setScanResult(null);
      };
      reader.readAsDataURL(file);
    }
  };

  return (
    <div className="h-full flex flex-col">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-slate-800 tracking-tight">Passport Scanner</h1>
        <p className="text-slate-500 mt-1">Gunakan kamera atau unggah file gambar paspor yang jelas.</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 flex-1">
        {/* Kiri: Area Kamera */}
        <div className="flex flex-col">
          <div className="relative bg-slate-900 rounded-3xl overflow-hidden shadow-2xl flex-1 max-h-[500px] flex items-center justify-center border-4 border-slate-800">
            {!capturedImage ? (
              <>
                <Webcam
                  audio={false}
                  ref={webcamRef}
                  screenshotFormat="image/jpeg"
                  videoConstraints={{ facingMode: "environment" }}
                  className="w-full h-full object-cover"
                />
                <div className="absolute inset-0 border-[6px] border-white/20 m-12 rounded-xl pointer-events-none"></div>
                <div className="absolute bottom-16 left-1/2 -translate-x-1/2 bg-black/60 backdrop-blur-md px-4 py-2 rounded-full text-white/90 text-sm font-medium tracking-wide">
                  Posisikan baris MRZ di sini
                </div>
              </>
            ) : (
              <img src={capturedImage} alt="Captured Passport" className="w-full h-full object-contain bg-slate-900 opacity-90" />
            )}
          </div>

          <div className="mt-6 flex flex-wrap gap-4 justify-center">
            {!capturedImage ? (
              <>
                <button
                  onClick={capture}
                  className="flex items-center gap-2 px-8 py-4 bg-blue-600 hover:bg-blue-700 text-white rounded-2xl font-semibold shadow-xl shadow-blue-600/20 transition-all active:scale-95"
                >
                  <Camera className="w-6 h-6" />
                  <span>Ambil Foto</span>
                </button>
                <label className="flex items-center gap-2 px-8 py-4 bg-white hover:bg-slate-50 text-slate-700 border border-slate-200 rounded-2xl font-semibold shadow-sm transition-all cursor-pointer">
                  <Upload className="w-6 h-6 text-slate-500" />
                  <span>Unggah File</span>
                  <input type="file" accept="image/*" className="hidden" onChange={handleFileUpload} />
                </label>
              </>
            ) : (
              <>
                <button
                  onClick={retake}
                  disabled={isProcessing}
                  className="flex items-center gap-2 px-6 py-4 bg-slate-200 hover:bg-slate-300 text-slate-700 rounded-2xl font-semibold transition-all disabled:opacity-50"
                >
                  <RefreshCw className="w-5 h-5" />
                  <span>Ulangi</span>
                </button>
                <button
                  onClick={uploadAndScan}
                  disabled={isProcessing}
                  className="flex items-center gap-2 px-8 py-4 bg-blue-600 hover:bg-blue-700 text-white rounded-2xl font-semibold shadow-xl shadow-blue-600/20 transition-all disabled:opacity-50"
                >
                  {isProcessing ? (
                    <RefreshCw className="w-6 h-6 animate-spin" />
                  ) : (
                    <Upload className="w-6 h-6" />
                  )}
                  <span>{isProcessing ? 'Memproses...' : 'Scan Sekarang'}</span>
                </button>
              </>
            )}
          </div>
        </div>

        {/* Kanan: Hasil Scan */}
        <div className="bg-white rounded-3xl p-8 border border-slate-200 shadow-sm flex flex-col">
          <div className="flex items-center gap-3 mb-6 pb-6 border-b border-slate-100">
            <FileText className="w-6 h-6 text-blue-600" />
            <h2 className="text-xl font-bold text-slate-800">Hasil Pemindaian</h2>
          </div>

          {!scanResult && !isProcessing && (
            <div className="flex-1 flex flex-col items-center justify-center text-center px-8">
              <div className="w-24 h-24 bg-slate-50 rounded-full flex items-center justify-center mb-4">
                <FileText className="w-10 h-10 text-slate-300" />
              </div>
              <h3 className="text-lg font-medium text-slate-700 mb-1">Belum ada data</h3>
              <p className="text-slate-500 text-sm">Ambil foto paspor untuk melihat hasil pemindaian di sini.</p>
            </div>
          )}

          {isProcessing && (
            <div className="flex-1 flex flex-col items-center justify-center text-center">
              <div className="w-16 h-16 border-4 border-blue-100 border-t-blue-600 rounded-full animate-spin mb-4"></div>
              <p className="text-blue-600 font-medium">Menganalisis Machine Readable Zone...</p>
            </div>
          )}

          {scanResult && (scanResult.guest || scanResult.partialData) && (
            <div className="flex-1 overflow-y-auto pr-2 custom-scrollbar">
              
              <div className={`p-4 rounded-xl mb-6 flex gap-3 ${scanResult.requiresManualReview ? 'bg-amber-50 text-amber-800 border border-amber-200' : 'bg-green-50 text-green-800 border border-green-200'}`}>
                {scanResult.requiresManualReview ? <AlertCircle className="w-6 h-6 shrink-0" /> : <CheckCircle2 className="w-6 h-6 shrink-0" />}
                <div>
                  <h4 className="font-semibold">{scanResult.requiresManualReview ? 'Perlu Tinjauan Manual' : 'Verifikasi Berhasil'}</h4>
                  <p className="text-sm opacity-90 mt-1">Confidence Score: <span className="font-bold">{scanResult.confidenceScore}%</span></p>
                </div>
              </div>

              <div className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div className="bg-slate-50 p-4 rounded-xl border border-slate-100">
                    <p className="text-xs text-slate-500 font-medium uppercase tracking-wider mb-1">Nama Belakang</p>
                    <p className="font-bold text-slate-800">{(scanResult.guest || scanResult.partialData).surname || '-'}</p>
                  </div>
                  <div className="bg-slate-50 p-4 rounded-xl border border-slate-100">
                    <p className="text-xs text-slate-500 font-medium uppercase tracking-wider mb-1">Nama Depan</p>
                    <p className="font-bold text-slate-800">{(scanResult.guest || scanResult.partialData).givenNames || '-'}</p>
                  </div>
                </div>

                <div className="bg-slate-50 p-4 rounded-xl border border-slate-100">
                  <p className="text-xs text-slate-500 font-medium uppercase tracking-wider mb-1">Nomor Paspor</p>
                  <p className="font-bold text-slate-800 text-lg tracking-widest">{(scanResult.guest || scanResult.partialData).passportNumber || '-'}</p>
                </div>

                <div className="grid grid-cols-3 gap-4">
                  <div className="bg-slate-50 p-4 rounded-xl border border-slate-100">
                    <p className="text-xs text-slate-500 font-medium uppercase tracking-wider mb-1">Negara</p>
                    <p className="font-bold text-slate-800">{(scanResult.guest || scanResult.partialData).issuingCountry || '-'}</p>
                  </div>
                  <div className="bg-slate-50 p-4 rounded-xl border border-slate-100">
                    <p className="text-xs text-slate-500 font-medium uppercase tracking-wider mb-1">Gender</p>
                    <p className="font-bold text-slate-800">{(scanResult.guest || scanResult.partialData).gender || '-'}</p>
                  </div>
                  <div className="bg-slate-50 p-4 rounded-xl border border-slate-100">
                    <p className="text-xs text-slate-500 font-medium uppercase tracking-wider mb-1">Nasionalitas</p>
                    <p className="font-bold text-slate-800">{(scanResult.guest || scanResult.partialData).nationality || '-'}</p>
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div className="bg-slate-50 p-4 rounded-xl border border-slate-100">
                    <p className="text-xs text-slate-500 font-medium uppercase tracking-wider mb-1">Tanggal Lahir</p>
                    <p className="font-bold text-slate-800">{(scanResult.guest || scanResult.partialData).dateOfBirth || '-'}</p>
                  </div>
                  <div className="bg-slate-50 p-4 rounded-xl border border-slate-100">
                    <p className="text-xs text-slate-500 font-medium uppercase tracking-wider mb-1">Berlaku Hingga</p>
                    <p className="font-bold text-slate-800">{(scanResult.guest || scanResult.partialData).expiryDate || '-'}</p>
                  </div>
                </div>

                {scanResult.requiresManualReview && (
                  <button className="w-full mt-4 py-3 bg-amber-500 hover:bg-amber-600 text-white rounded-xl font-semibold transition-colors">
                    Koreksi Data Manual
                  </button>
                )}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;
