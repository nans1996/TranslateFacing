using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.ML;
using MlNetPhotoModelML.Model;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.IO;

namespace MlNetPhotoModel.Controllers
{
    public class TranslateController : Controller
    {
        [HttpPost("api/Translate")]
        public string Translate([FromBody] ModelInput obj)
        {
            MLContext mlContext = new MLContext();
            ITransformer mlModel = mlContext.Model.Load("MLModel.zip", out var modelInputSchema);
            var predEngine = mlContext.Model.CreatePredictionEngine<ModelInput, ModelOutput>(mlModel, false);

            List<string> emotion_labels = new List<string>() { "angry", "digust", "fear", "happy", "neitral", "not_face", "sad", "surprice" };
           // string folder_path_file_result = @"D:\test_person\result_file.csv";
           // System.IO.File.AppendAllText(folder_path_file_result, "label    result_translate    path    max_score");
           ModelOutput result = predEngine.Predict(obj);
            string res = $"Image is {result.Prediction} a score {Math.Round(result.Score.Max()*100,1)}% " + Environment.NewLine;
            for (int i=0; i < result.Score.Length; i++)
            {
                var max = result.Score.Max();

                if (emotion_labels[i] != result.Prediction && emotion_labels[i] != "not_face")
                {
                    res += $"{emotion_labels[i]} a score {Math.Round(result.Score[i]*100,1)}% " + Environment.NewLine;
                }
               
            }
            return res;
        }

    }
}
